/*
 * Copyright 2021 The Android Open Source Project
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

package com.yuetsau.kawakarpo.feature.topic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yuetsau.kawakarpo.core.data.repository.NewsResourceQuery
import com.yuetsau.kawakarpo.core.data.repository.TopicsRepository
import com.yuetsau.kawakarpo.core.data.repository.UserDataRepository
import com.yuetsau.kawakarpo.core.data.repository.UserNewsResourceRepository
import com.yuetsau.kawakarpo.core.model.data.FollowableTopic
import com.yuetsau.kawakarpo.core.model.data.Topic
import com.yuetsau.kawakarpo.core.model.data.UserNewsResource
import com.yuetsau.kawakarpo.core.result.Result
import com.yuetsau.kawakarpo.core.result.asResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = TopicViewModel.Factory::class)
class TopicViewModel @AssistedInject constructor(
    private val userDataRepository: UserDataRepository,
    topicsRepository: TopicsRepository,
    userNewsResourceRepository: UserNewsResourceRepository,
    @Assisted val topicId: String,
) : ViewModel() {
    val topicUiState: StateFlow<TopicUiState> = topicUiState(
        topicId = topicId,
        userDataRepository = userDataRepository,
        topicsRepository = topicsRepository,
    )
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TopicUiState.Loading,
        )

    val newsUiState: StateFlow<NewsUiState> = newsUiState(
        topicId = topicId,
        userDataRepository = userDataRepository,
        userNewsResourceRepository = userNewsResourceRepository,
    )
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NewsUiState.Loading,
        )

    fun followTopicToggle(followed: Boolean) {
        viewModelScope.launch {
            userDataRepository.setTopicIdFollowed(topicId, followed)
        }
    }

    fun bookmarkNews(newsResourceId: String, bookmarked: Boolean) {
        viewModelScope.launch {
            userDataRepository.setNewsResourceBookmarked(newsResourceId, bookmarked)
        }
    }

    fun setNewsResourceViewed(newsResourceId: String, viewed: Boolean) {
        viewModelScope.launch {
            userDataRepository.setNewsResourceViewed(newsResourceId, viewed)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            topicId: String,
        ): TopicViewModel
    }
}

private fun topicUiState(
    topicId: String,
    userDataRepository: UserDataRepository,
    topicsRepository: TopicsRepository,
): Flow<TopicUiState> {
    // Observe the followed topics, as they could change over time.
    val followedTopicIds: Flow<Set<String>> =
        userDataRepository.userData
            .map { it.followedTopics }

    // Observe topic information
    val topicStream: Flow<Topic> = topicsRepository.getTopic(
        id = topicId,
    )

    return combine(
        followedTopicIds,
        topicStream,
        ::Pair,
    )
        .asResult()
        .map { followedTopicToTopicResult ->
            when (followedTopicToTopicResult) {
                is Result.Success -> {
                    val (followedTopics, topic) = followedTopicToTopicResult.data
                    TopicUiState.Success(
                        followableTopic = FollowableTopic(
                            topic = topic,
                            isFollowed = topicId in followedTopics,
                        ),
                    )
                }

                is Result.Loading -> TopicUiState.Loading
                is Result.Error -> TopicUiState.Error
            }
        }
}

private fun newsUiState(
    topicId: String,
    userNewsResourceRepository: UserNewsResourceRepository,
    userDataRepository: UserDataRepository,
): Flow<NewsUiState> {
    // Observe news
    val newsStream: Flow<List<UserNewsResource>> = userNewsResourceRepository.observeAll(
        NewsResourceQuery(filterTopicIds = setOf(element = topicId)),
    )

    // Observe bookmarks
    val bookmark: Flow<Set<String>> = userDataRepository.userData
        .map { it.bookmarkedNewsResources }

    return combine(newsStream, bookmark, ::Pair)
        .asResult()
        .map { newsToBookmarksResult ->
            when (newsToBookmarksResult) {
                is Result.Success -> NewsUiState.Success(newsToBookmarksResult.data.first)
                is Result.Loading -> NewsUiState.Loading
                is Result.Error -> NewsUiState.Error
            }
        }
}

sealed interface TopicUiState {
    data class Success(val followableTopic: FollowableTopic) : TopicUiState
    data object Error : TopicUiState
    data object Loading : TopicUiState
}

sealed interface NewsUiState {
    data class Success(val news: List<UserNewsResource>) : NewsUiState
    data object Error : NewsUiState
    data object Loading : NewsUiState
}
