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

package com.yuetsau.kawakarpo.interests

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.yuetsau.kawakarpo.core.testing.data.followableTopicTestData
import com.yuetsau.kawakarpo.feature.interests.InterestsScreen
import com.yuetsau.kawakarpo.feature.interests.InterestsUiState
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.yuetsau.kawakarpo.core.ui.R as CoreUiR
import com.yuetsau.kawakarpo.feature.interests.R as InterestsR

/**
 * UI test for checking the correct behaviour of the Interests screen;
 * Verifies that, when a specific UiState is set, the corresponding
 * composables and details are shown
 */
class InterestsScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var interestsLoading: String
    private lateinit var interestsEmptyHeader: String
    private lateinit var interestsTopicCardFollowButton: String
    private lateinit var interestsTopicCardUnfollowButton: String

    @Before
    fun setup() {
        composeTestRule.activity.apply {
            interestsLoading = getString(InterestsR.string.feature_interests_loading)
            interestsEmptyHeader = getString(InterestsR.string.feature_interests_empty_header)
            interestsTopicCardFollowButton =
                getString(CoreUiR.string.core_ui_interests_card_follow_button_content_desc)
            interestsTopicCardUnfollowButton =
                getString(CoreUiR.string.core_ui_interests_card_unfollow_button_content_desc)
        }
    }

    @Test
    fun niaLoadingWheel_inTopics_whenScreenIsLoading_showLoading() {
        composeTestRule.setContent {
            InterestsScreen(uiState = InterestsUiState.Loading)
        }

        composeTestRule
            .onNodeWithContentDescription(interestsLoading)
            .assertExists()
    }

    @Test
    fun interestsWithTopics_whenTopicsFollowed_showFollowedAndUnfollowedTopicsWithInfo() {
        composeTestRule.setContent {
            InterestsScreen(
                uiState = InterestsUiState.Interests(
                    topics = followableTopicTestData,
                    selectedTopicId = null,
                ),
            )
        }

        composeTestRule
            .onNodeWithText(followableTopicTestData[0].topic.name)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(followableTopicTestData[1].topic.name)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(followableTopicTestData[2].topic.name)
            .assertIsDisplayed()

        composeTestRule
            .onAllNodesWithContentDescription(interestsTopicCardFollowButton)
            .assertCountEquals(numberOfUnfollowedTopics)
    }

    @Test
    fun topicsEmpty_whenDataIsEmptyOccurs_thenShowEmptyScreen() {
        composeTestRule.setContent {
            InterestsScreen(uiState = InterestsUiState.Empty)
        }

        composeTestRule
            .onNodeWithText(interestsEmptyHeader)
            .assertIsDisplayed()
    }

    @Composable
    private fun InterestsScreen(uiState: InterestsUiState) {
        InterestsScreen(
            uiState = uiState,
            followTopic = { _, _ -> },
            onTopicClick = {},
        )
    }
}

private val numberOfUnfollowedTopics = followableTopicTestData.filter { !it.isFollowed }.size
