/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yuetsau.kawakarpo.core.data.repository

import com.yuetsau.kawakarpo.core.data.Synchronizer
import com.yuetsau.kawakarpo.core.data.model.asEntity
import com.yuetsau.kawakarpo.core.data.testdoubles.CollectionType
import com.yuetsau.kawakarpo.core.data.testdoubles.TestNiaNetworkDataSource
import com.yuetsau.kawakarpo.core.data.testdoubles.TestTopicDao
import com.yuetsau.kawakarpo.core.database.dao.TopicDao
import com.yuetsau.kawakarpo.core.database.model.TopicEntity
import com.yuetsau.kawakarpo.core.database.model.asExternalModel
import com.yuetsau.kawakarpo.core.datastore.NiaPreferencesDataSource
import com.yuetsau.kawakarpo.core.datastore.UserPreferences
import com.yuetsau.kawakarpo.core.datastore.test.InMemoryDataStore
import com.yuetsau.kawakarpo.core.model.data.Topic
import com.yuetsau.kawakarpo.core.network.model.NetworkTopic
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class OfflineFirstTopicsRepositoryTest {

    private val testScope = TestScope(UnconfinedTestDispatcher())

    private lateinit var subject: OfflineFirstTopicsRepository

    private lateinit var topicDao: TopicDao

    private lateinit var network: TestNiaNetworkDataSource

    private lateinit var niaPreferences: NiaPreferencesDataSource

    private lateinit var synchronizer: Synchronizer

    @Before
    fun setup() {
        topicDao = TestTopicDao()
        network = TestNiaNetworkDataSource()
        niaPreferences = NiaPreferencesDataSource(InMemoryDataStore(UserPreferences.getDefaultInstance()))
        synchronizer = TestSynchronizer(niaPreferences)

        subject = OfflineFirstTopicsRepository(
            topicDao = topicDao,
            network = network,
        )
    }

    @Test
    fun offlineFirstTopicsRepository_topics_stream_is_backed_by_topics_dao() =
        testScope.runTest {
            subject.syncWith(synchronizer)

            assertEquals(
                topicDao.getTopicEntities()
                    .first()
                    .map(TopicEntity::asExternalModel),
                subject.getTopics()
                    .first(),
            )
        }

    @Test
    fun offlineFirstTopicsRepository_sync_pulls_from_network() =
        testScope.runTest {
            subject.syncWith(synchronizer)

            val networkTopics = network.getTopics()
                .map(NetworkTopic::asEntity)

            val dbTopics = topicDao.getTopicEntities()
                .first()

            assertEquals(
                networkTopics.map(TopicEntity::id),
                dbTopics.map(TopicEntity::id),
            )

            // After sync version should be updated
            assertEquals(
                network.latestChangeListVersion(CollectionType.Topics),
                synchronizer.getChangeListVersions().topicVersion,
            )
        }

    @Test
    fun offlineFirstTopicsRepository_incremental_sync_pulls_from_network() =
        testScope.runTest {
            // Set topics version to 10
            synchronizer.updateChangeListVersions {
                copy(topicVersion = 10)
            }

            subject.syncWith(synchronizer)

            val networkTopics = network.getTopics()
                .map(NetworkTopic::asEntity)
                // Drop 10 to simulate the first 10 items being unchanged
                .drop(10)

            val dbTopics = topicDao.getTopicEntities()
                .first()

            assertEquals(
                networkTopics.map(TopicEntity::id),
                dbTopics.map(TopicEntity::id),
            )

            // After sync version should be updated
            assertEquals(
                network.latestChangeListVersion(CollectionType.Topics),
                synchronizer.getChangeListVersions().topicVersion,
            )
        }

    @Test
    fun offlineFirstTopicsRepository_sync_deletes_items_marked_deleted_on_network() =
        testScope.runTest {
            val networkTopics = network.getTopics()
                .map(NetworkTopic::asEntity)
                .map(TopicEntity::asExternalModel)

            // Delete half of the items on the network
            val deletedItems = networkTopics
                .map(Topic::id)
                .partition { it.chars().sum() % 2 == 0 }
                .first
                .toSet()

            deletedItems.forEach {
                network.editCollection(
                    collectionType = CollectionType.Topics,
                    id = it,
                    isDelete = true,
                )
            }

            subject.syncWith(synchronizer)

            val dbTopics = topicDao.getTopicEntities()
                .first()
                .map(TopicEntity::asExternalModel)

            // Assert that items marked deleted on the network have been deleted locally
            assertEquals(
                networkTopics.map(Topic::id) - deletedItems,
                dbTopics.map(Topic::id),
            )

            // After sync version should be updated
            assertEquals(
                network.latestChangeListVersion(CollectionType.Topics),
                synchronizer.getChangeListVersions().topicVersion,
            )
        }
}
