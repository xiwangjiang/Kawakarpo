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

package com.yuetsau.kawakarpo.core.data.di

import com.yuetsau.kawakarpo.core.data.repository.DefaultRecentSearchRepository
import com.yuetsau.kawakarpo.core.data.repository.DefaultSearchContentsRepository
import com.yuetsau.kawakarpo.core.data.repository.NewsRepository
import com.yuetsau.kawakarpo.core.data.repository.OfflineFirstNewsRepository
import com.yuetsau.kawakarpo.core.data.repository.OfflineFirstTopicsRepository
import com.yuetsau.kawakarpo.core.data.repository.OfflineFirstUserDataRepository
import com.yuetsau.kawakarpo.core.data.repository.RecentSearchRepository
import com.yuetsau.kawakarpo.core.data.repository.SearchContentsRepository
import com.yuetsau.kawakarpo.core.data.repository.TopicsRepository
import com.yuetsau.kawakarpo.core.data.repository.UserDataRepository
import com.yuetsau.kawakarpo.core.data.util.ConnectivityManagerNetworkMonitor
import com.yuetsau.kawakarpo.core.data.util.NetworkMonitor
import com.yuetsau.kawakarpo.core.data.util.TimeZoneBroadcastMonitor
import com.yuetsau.kawakarpo.core.data.util.TimeZoneMonitor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    internal abstract fun bindsTopicRepository(
        topicsRepository: OfflineFirstTopicsRepository,
    ): TopicsRepository

    @Binds
    internal abstract fun bindsNewsResourceRepository(
        newsRepository: OfflineFirstNewsRepository,
    ): NewsRepository

    @Binds
    internal abstract fun bindsUserDataRepository(
        userDataRepository: OfflineFirstUserDataRepository,
    ): UserDataRepository

    @Binds
    internal abstract fun bindsRecentSearchRepository(
        recentSearchRepository: DefaultRecentSearchRepository,
    ): RecentSearchRepository

    @Binds
    internal abstract fun bindsSearchContentsRepository(
        searchContentsRepository: DefaultSearchContentsRepository,
    ): SearchContentsRepository

    @Binds
    internal abstract fun bindsNetworkMonitor(
        networkMonitor: ConnectivityManagerNetworkMonitor,
    ): NetworkMonitor

    @Binds
    internal abstract fun binds(impl: TimeZoneBroadcastMonitor): TimeZoneMonitor
}
