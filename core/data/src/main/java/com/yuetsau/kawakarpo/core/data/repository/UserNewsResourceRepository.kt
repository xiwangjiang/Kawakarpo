/*
 * Copyright 2023 The Android Open Source Project
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

import com.yuetsau.kawakarpo.core.model.data.UserNewsResource
import kotlinx.coroutines.flow.Flow

/**
 * Data layer implementation for [UserNewsResource]
 */
interface UserNewsResourceRepository {
    /**
     * Returns available news resources as a stream.
     */
    fun observeAll(
        query: NewsResourceQuery = NewsResourceQuery(
            filterTopicIds = null,
            filterNewsIds = null,
        ),
    ): Flow<List<UserNewsResource>>

    /**
     * Returns available news resources for the user's followed topics as a stream.
     */
    fun observeAllForFollowedTopics(): Flow<List<UserNewsResource>>

    /**
     * Returns the user's bookmarked news resources as a stream.
     */
    fun observeAllBookmarked(): Flow<List<UserNewsResource>>
}
