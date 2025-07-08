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

package com.yuetsau.kawakarpo.core.data.model

import com.yuetsau.kawakarpo.core.database.model.NewsResourceEntity
import com.yuetsau.kawakarpo.core.database.model.NewsResourceTopicCrossRef
import com.yuetsau.kawakarpo.core.database.model.TopicEntity
import com.yuetsau.kawakarpo.core.model.data.NewsResource
import com.yuetsau.kawakarpo.core.network.model.NetworkNewsResource
import com.yuetsau.kawakarpo.core.network.model.NetworkTopic
import com.yuetsau.kawakarpo.core.network.model.asExternalModel

fun NetworkNewsResource.asEntity() = NewsResourceEntity(
    id = id,
    title = title,
    content = content,
    url = url,
    headerImageUrl = headerImageUrl,
    publishDate = publishDate,
    type = type,
)

/**
 * A shell [TopicEntity] to fulfill the foreign key constraint when inserting
 * a [NewsResourceEntity] into the DB
 */
fun NetworkNewsResource.topicEntityShells() =
    topics.map { topicId ->
        TopicEntity(
            id = topicId,
            name = "",
            url = "",
            imageUrl = "",
            shortDescription = "",
            longDescription = "",
        )
    }

fun NetworkNewsResource.topicCrossReferences(): List<NewsResourceTopicCrossRef> =
    topics.map { topicId ->
        NewsResourceTopicCrossRef(
            newsResourceId = id,
            topicId = topicId,
        )
    }

fun NetworkNewsResource.asExternalModel(topics: List<NetworkTopic>) =
    NewsResource(
        id = id,
        title = title,
        content = content,
        url = url,
        headerImageUrl = headerImageUrl,
        publishDate = publishDate,
        type = type,
        topics = topics
            .filter { networkTopic -> this.topics.contains(networkTopic.id) }
            .map(NetworkTopic::asExternalModel),
    )
