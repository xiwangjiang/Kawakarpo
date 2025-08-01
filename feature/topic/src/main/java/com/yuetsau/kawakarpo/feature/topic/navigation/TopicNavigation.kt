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

package com.yuetsau.kawakarpo.feature.topic.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.yuetsau.kawakarpo.feature.topic.TopicScreen
import com.yuetsau.kawakarpo.feature.topic.TopicViewModel
import kotlinx.serialization.Serializable

@Serializable data class TopicRoute(val id: String)

fun NavController.navigateToTopic(topicId: String, navOptions: NavOptionsBuilder.() -> Unit = {}) {
    navigate(route = TopicRoute(topicId)) {
        navOptions()
    }
}

fun NavGraphBuilder.topicScreen(
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onTopicClick: (String) -> Unit,
) {
    composable<TopicRoute> { entry ->
        val id = entry.toRoute<TopicRoute>().id
        TopicScreen(
            showBackButton = showBackButton,
            onBackClick = onBackClick,
            onTopicClick = onTopicClick,
            viewModel = hiltViewModel<TopicViewModel, TopicViewModel.Factory>(
                key = id,
            ) { factory ->
                factory.create(id)
            },
        )
    }
}
