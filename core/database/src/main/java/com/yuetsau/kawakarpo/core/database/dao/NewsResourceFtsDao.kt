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

package com.yuetsau.kawakarpo.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yuetsau.kawakarpo.core.database.model.NewsResourceFtsEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for [NewsResourceFtsEntity] access.
 */
@Dao
interface NewsResourceFtsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(newsResources: List<NewsResourceFtsEntity>)

    @Query("SELECT newsResourceId FROM newsResourcesFts WHERE newsResourcesFts MATCH :query")
    fun searchAllNewsResources(query: String): Flow<List<String>>

    @Query("SELECT count(*) FROM newsResourcesFts")
    fun getCount(): Flow<Int>
}
