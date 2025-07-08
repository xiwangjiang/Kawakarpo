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

package com.yuetsau.kawakarpo.core.data.test

import com.yuetsau.kawakarpo.core.data.di.DataModule
import com.yuetsau.kawakarpo.core.data.repository.NewsRepository
import com.yuetsau.kawakarpo.core.data.repository.RecentSearchRepository
import com.yuetsau.kawakarpo.core.data.repository.SearchContentsRepository
import com.yuetsau.kawakarpo.core.data.repository.TopicsRepository
import com.yuetsau.kawakarpo.core.data.repository.UserDataRepository
import com.yuetsau.kawakarpo.core.data.test.repository.FakeNewsRepository
import com.yuetsau.kawakarpo.core.data.test.repository.FakeRecentSearchRepository
import com.yuetsau.kawakarpo.core.data.test.repository.FakeSearchContentsRepository
import com.yuetsau.kawakarpo.core.data.test.repository.FakeTopicsRepository
import com.yuetsau.kawakarpo.core.data.test.repository.FakeUserDataRepository
import com.yuetsau.kawakarpo.core.data.util.NetworkMonitor
import com.yuetsau.kawakarpo.core.data.util.TimeZoneMonitor
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DataModule::class],
)
internal interface TestDataModule {
    @Binds
    fun bindsTopicRepository(
        fakeTopicsRepository: FakeTopicsRepository,
    ): TopicsRepository

    @Binds
    fun bindsNewsResourceRepository(
        fakeNewsRepository: FakeNewsRepository,
    ): NewsRepository

    @Binds
    fun bindsUserDataRepository(
        userDataRepository: FakeUserDataRepository,
    ): UserDataRepository

    @Binds
    fun bindsRecentSearchRepository(
        recentSearchRepository: FakeRecentSearchRepository,
    ): RecentSearchRepository

    @Binds
    fun bindsSearchContentsRepository(
        searchContentsRepository: FakeSearchContentsRepository,
    ): SearchContentsRepository

    @Binds
    fun bindsNetworkMonitor(
        networkMonitor: AlwaysOnlineNetworkMonitor,
    ): NetworkMonitor

    @Binds
    fun binds(impl: DefaultZoneIdTimeZoneMonitor): TimeZoneMonitor
}
