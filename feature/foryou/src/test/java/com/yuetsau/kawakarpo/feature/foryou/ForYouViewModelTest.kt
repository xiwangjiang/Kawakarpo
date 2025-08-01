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

package com.yuetsau.kawakarpo.feature.foryou

import androidx.lifecycle.SavedStateHandle
import com.yuetsau.kawakarpo.core.analytics.AnalyticsEvent
import com.yuetsau.kawakarpo.core.analytics.AnalyticsEvent.Param
import com.yuetsau.kawakarpo.core.data.repository.CompositeUserNewsResourceRepository
import com.yuetsau.kawakarpo.core.domain.GetFollowableTopicsUseCase
import com.yuetsau.kawakarpo.core.model.data.FollowableTopic
import com.yuetsau.kawakarpo.core.model.data.NewsResource
import com.yuetsau.kawakarpo.core.model.data.Topic
import com.yuetsau.kawakarpo.core.model.data.UserNewsResource
import com.yuetsau.kawakarpo.core.model.data.mapToUserNewsResources
import com.yuetsau.kawakarpo.core.notifications.DEEP_LINK_NEWS_RESOURCE_ID_KEY
import com.yuetsau.kawakarpo.core.testing.repository.TestNewsRepository
import com.yuetsau.kawakarpo.core.testing.repository.TestTopicsRepository
import com.yuetsau.kawakarpo.core.testing.repository.TestUserDataRepository
import com.yuetsau.kawakarpo.core.testing.repository.emptyUserData
import com.yuetsau.kawakarpo.core.testing.util.MainDispatcherRule
import com.yuetsau.kawakarpo.core.testing.util.TestAnalyticsHelper
import com.yuetsau.kawakarpo.core.testing.util.TestSyncManager
import com.yuetsau.kawakarpo.core.ui.NewsFeedUiState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * To learn more about how this test handles Flows created with stateIn, see
 * https://developer.android.com/kotlin/flow/test#statein
 */
class ForYouViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val syncManager = TestSyncManager()
    private val analyticsHelper = TestAnalyticsHelper()
    private val userDataRepository = TestUserDataRepository()
    private val topicsRepository = TestTopicsRepository()
    private val newsRepository = TestNewsRepository()
    private val userNewsResourceRepository = CompositeUserNewsResourceRepository(
        newsRepository = newsRepository,
        userDataRepository = userDataRepository,
    )

    private val getFollowableTopicsUseCase = GetFollowableTopicsUseCase(
        topicsRepository = topicsRepository,
        userDataRepository = userDataRepository,
    )

    private val savedStateHandle = SavedStateHandle()
    private lateinit var viewModel: ForYouViewModel

    @Before
    fun setup() {
        viewModel = ForYouViewModel(
            syncManager = syncManager,
            savedStateHandle = savedStateHandle,
            analyticsHelper = analyticsHelper,
            userDataRepository = userDataRepository,
            userNewsResourceRepository = userNewsResourceRepository,
            getFollowableTopics = getFollowableTopicsUseCase,
        )
    }

    @Test
    fun stateIsInitiallyLoading() = runTest {
        assertEquals(
            OnboardingUiState.Loading,
            viewModel.onboardingUiState.value,
        )
        assertEquals(NewsFeedUiState.Loading, viewModel.feedState.value)
    }

    @Test
    fun stateIsLoadingWhenFollowedTopicsAreLoading() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.onboardingUiState.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.feedState.collect() }

        topicsRepository.sendTopics(sampleTopics)

        assertEquals(
            OnboardingUiState.Loading,
            viewModel.onboardingUiState.value,
        )
        assertEquals(NewsFeedUiState.Loading, viewModel.feedState.value)
    }

    @Test
    fun stateIsLoadingWhenAppIsSyncingWithNoInterests() = runTest {
        syncManager.setSyncing(true)

        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.isSyncing.collect() }

        assertEquals(
            true,
            viewModel.isSyncing.value,
        )
    }

    @Test
    fun onboardingStateIsLoadingWhenTopicsAreLoading() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.onboardingUiState.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.feedState.collect() }

        userDataRepository.setFollowedTopicIds(emptySet())

        assertEquals(
            OnboardingUiState.Loading,
            viewModel.onboardingUiState.value,
        )
        assertEquals(NewsFeedUiState.Success(emptyList()), viewModel.feedState.value)
    }

    @Test
    fun onboardingIsShownWhenNewsResourcesAreLoading() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.onboardingUiState.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.feedState.collect() }

        topicsRepository.sendTopics(sampleTopics)
        userDataRepository.setFollowedTopicIds(emptySet())

        assertEquals(
            OnboardingUiState.Shown(
                topics = listOf(
                    FollowableTopic(
                        topic = Topic(
                            id = "0",
                            name = "Headlines",
                            shortDescription = "",
                            longDescription = "long description",
                            url = "URL",
                            imageUrl = "image URL",
                        ),
                        isFollowed = false,
                    ),
                    FollowableTopic(
                        topic = Topic(
                            id = "1",
                            name = "UI",
                            shortDescription = "",
                            longDescription = "long description",
                            url = "URL",
                            imageUrl = "image URL",
                        ),
                        isFollowed = false,
                    ),
                    FollowableTopic(
                        topic = Topic(
                            id = "2",
                            name = "Tools",
                            shortDescription = "",
                            longDescription = "long description",
                            url = "URL",
                            imageUrl = "image URL",
                        ),
                        isFollowed = false,
                    ),
                ),
            ),
            viewModel.onboardingUiState.value,
        )
        assertEquals(
            NewsFeedUiState.Success(
                feed = emptyList(),
            ),
            viewModel.feedState.value,
        )
    }

    @Test
    fun onboardingIsShownAfterLoadingEmptyFollowedTopics() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.onboardingUiState.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.feedState.collect() }

        topicsRepository.sendTopics(sampleTopics)
        userDataRepository.setFollowedTopicIds(emptySet())
        newsRepository.sendNewsResources(sampleNewsResources)

        assertEquals(
            OnboardingUiState.Shown(
                topics = listOf(
                    FollowableTopic(
                        topic = Topic(
                            id = "0",
                            name = "Headlines",
                            shortDescription = "",
                            longDescription = "long description",
                            url = "URL",
                            imageUrl = "image URL",
                        ),
                        isFollowed = false,
                    ),
                    FollowableTopic(
                        topic = Topic(
                            id = "1",
                            name = "UI",
                            shortDescription = "",
                            longDescription = "long description",
                            url = "URL",
                            imageUrl = "image URL",
                        ),
                        isFollowed = false,
                    ),
                    FollowableTopic(
                        topic = Topic(
                            id = "2",
                            name = "Tools",
                            shortDescription = "",
                            longDescription = "long description",
                            url = "URL",
                            imageUrl = "image URL",
                        ),
                        isFollowed = false,
                    ),
                ),
            ),
            viewModel.onboardingUiState.value,
        )
        assertEquals(
            NewsFeedUiState.Success(
                feed = emptyList(),
            ),
            viewModel.feedState.value,
        )
    }

    @Test
    fun onboardingIsNotShownAfterUserDismissesOnboarding() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.onboardingUiState.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.feedState.collect() }

        topicsRepository.sendTopics(sampleTopics)

        val followedTopicIds = setOf("0", "1")
        val userData = emptyUserData.copy(followedTopics = followedTopicIds)
        userDataRepository.setUserData(userData)
        viewModel.dismissOnboarding()

        assertEquals(
            OnboardingUiState.NotShown,
            viewModel.onboardingUiState.value,
        )
        assertEquals(NewsFeedUiState.Loading, viewModel.feedState.value)

        newsRepository.sendNewsResources(sampleNewsResources)

        assertEquals(
            OnboardingUiState.NotShown,
            viewModel.onboardingUiState.value,
        )
        assertEquals(
            NewsFeedUiState.Success(
                feed = sampleNewsResources.mapToUserNewsResources(userData),
            ),
            viewModel.feedState.value,
        )
    }

    @Test
    fun topicSelectionUpdatesAfterSelectingTopic() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.onboardingUiState.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.feedState.collect() }

        topicsRepository.sendTopics(sampleTopics)
        userDataRepository.setFollowedTopicIds(emptySet())
        newsRepository.sendNewsResources(sampleNewsResources)

        assertEquals(
            OnboardingUiState.Shown(
                topics = sampleTopics.map {
                    FollowableTopic(it, false)
                },
            ),
            viewModel.onboardingUiState.value,
        )
        assertEquals(
            NewsFeedUiState.Success(
                feed = emptyList(),
            ),
            viewModel.feedState.value,
        )

        val followedTopicId = sampleTopics[1].id
        viewModel.updateTopicSelection(followedTopicId, isChecked = true)

        assertEquals(
            OnboardingUiState.Shown(
                topics = sampleTopics.map {
                    FollowableTopic(it, it.id == followedTopicId)
                },
            ),
            viewModel.onboardingUiState.value,
        )

        val userData = emptyUserData.copy(followedTopics = setOf(followedTopicId))

        assertEquals(
            NewsFeedUiState.Success(
                feed = listOf(
                    UserNewsResource(sampleNewsResources[1], userData),
                    UserNewsResource(sampleNewsResources[2], userData),
                ),
            ),
            viewModel.feedState.value,
        )
    }

    @Test
    fun topicSelectionUpdatesAfterUnselectingTopic() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.onboardingUiState.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.feedState.collect() }

        topicsRepository.sendTopics(sampleTopics)
        userDataRepository.setFollowedTopicIds(emptySet())
        newsRepository.sendNewsResources(sampleNewsResources)
        viewModel.updateTopicSelection("1", isChecked = true)
        viewModel.updateTopicSelection("1", isChecked = false)

        advanceUntilIdle()
        assertEquals(
            OnboardingUiState.Shown(
                topics = listOf(
                    FollowableTopic(
                        topic = Topic(
                            id = "0",
                            name = "Headlines",
                            shortDescription = "",
                            longDescription = "long description",
                            url = "URL",
                            imageUrl = "image URL",
                        ),
                        isFollowed = false,
                    ),
                    FollowableTopic(
                        topic = Topic(
                            id = "1",
                            name = "UI",
                            shortDescription = "",
                            longDescription = "long description",
                            url = "URL",
                            imageUrl = "image URL",
                        ),
                        isFollowed = false,
                    ),
                    FollowableTopic(
                        topic = Topic(
                            id = "2",
                            name = "Tools",
                            shortDescription = "",
                            longDescription = "long description",
                            url = "URL",
                            imageUrl = "image URL",
                        ),
                        isFollowed = false,
                    ),
                ),
            ),
            viewModel.onboardingUiState.value,
        )
        assertEquals(
            NewsFeedUiState.Success(
                feed = emptyList(),
            ),
            viewModel.feedState.value,
        )
    }

    @Test
    fun newsResourceSelectionUpdatesAfterLoadingFollowedTopics() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.onboardingUiState.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.feedState.collect() }

        val followedTopicIds = setOf("1")
        val userData = emptyUserData.copy(
            followedTopics = followedTopicIds,
            shouldHideOnboarding = true,
        )

        topicsRepository.sendTopics(sampleTopics)
        userDataRepository.setUserData(userData)
        newsRepository.sendNewsResources(sampleNewsResources)

        val bookmarkedNewsResourceId = "2"
        viewModel.updateNewsResourceSaved(
            newsResourceId = bookmarkedNewsResourceId,
            isChecked = true,
        )

        val userDataExpected = userData.copy(
            bookmarkedNewsResources = setOf(bookmarkedNewsResourceId),
        )

        assertEquals(
            OnboardingUiState.NotShown,
            viewModel.onboardingUiState.value,
        )
        assertEquals(
            NewsFeedUiState.Success(
                feed = listOf(
                    UserNewsResource(newsResource = sampleNewsResources[1], userDataExpected),
                    UserNewsResource(newsResource = sampleNewsResources[2], userDataExpected),
                ),
            ),
            viewModel.feedState.value,
        )
    }

    @Test
    fun deepLinkedNewsResourceIsFetchedAndResetAfterViewing() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.deepLinkedNewsResource.collect() }

        newsRepository.sendNewsResources(sampleNewsResources)
        userDataRepository.setUserData(emptyUserData)
        savedStateHandle[DEEP_LINK_NEWS_RESOURCE_ID_KEY] = sampleNewsResources.first().id

        assertEquals(
            expected = UserNewsResource(
                newsResource = sampleNewsResources.first(),
                userData = emptyUserData,
            ),
            actual = viewModel.deepLinkedNewsResource.value,
        )

        viewModel.onDeepLinkOpened(
            newsResourceId = sampleNewsResources.first().id,
        )

        assertNull(
            viewModel.deepLinkedNewsResource.value,
        )

        assertTrue(
            analyticsHelper.hasLogged(
                AnalyticsEvent(
                    type = "news_deep_link_opened",
                    extras = listOf(
                        Param(
                            key = DEEP_LINK_NEWS_RESOURCE_ID_KEY,
                            value = sampleNewsResources.first().id,
                        ),
                    ),
                ),
            ),
        )
    }

    @Test
    fun whenUpdateNewsResourceSavedIsCalled_bookmarkStateIsUpdated() = runTest {
        val newsResourceId = "123"
        viewModel.updateNewsResourceSaved(newsResourceId, true)

        assertEquals(
            expected = setOf(newsResourceId),
            actual = userDataRepository.userData.first().bookmarkedNewsResources,
        )

        viewModel.updateNewsResourceSaved(newsResourceId, false)

        assertEquals(
            expected = emptySet(),
            actual = userDataRepository.userData.first().bookmarkedNewsResources,
        )
    }
}

private val sampleTopics = listOf(
    Topic(
        id = "0",
        name = "Headlines",
        shortDescription = "",
        longDescription = "long description",
        url = "URL",
        imageUrl = "image URL",
    ),
    Topic(
        id = "1",
        name = "UI",
        shortDescription = "",
        longDescription = "long description",
        url = "URL",
        imageUrl = "image URL",
    ),
    Topic(
        id = "2",
        name = "Tools",
        shortDescription = "",
        longDescription = "long description",
        url = "URL",
        imageUrl = "image URL",
    ),
)

private val sampleNewsResources = listOf(
    NewsResource(
        id = "1",
        title = "Thanks for helping us reach 1M YouTube Subscribers",
        content = "Thank you everyone for following the Now in Android series and everything the " +
            "Android Developers YouTube channel has to offer. During the Android Developer " +
            "Summit, our YouTube channel reached 1 million subscribers! Here’s a small video to " +
            "thank you all.",
        url = "https://youtu.be/-fJ6poHQrjM",
        headerImageUrl = "https://i.ytimg.com/vi/-fJ6poHQrjM/maxresdefault.jpg",
        publishDate = Instant.parse("2021-11-09T00:00:00.000Z"),
        type = "Video 📺",
        topics = listOf(
            Topic(
                id = "0",
                name = "Headlines",
                shortDescription = "",
                longDescription = "long description",
                url = "URL",
                imageUrl = "image URL",
            ),
        ),
    ),
    NewsResource(
        id = "2",
        title = "Transformations and customisations in the Paging Library",
        content = "A demonstration of different operations that can be performed with Paging. " +
            "Transformations like inserting separators, when to create a new pager, and " +
            "customisation options for consuming PagingData.",
        url = "https://youtu.be/ZARz0pjm5YM",
        headerImageUrl = "https://i.ytimg.com/vi/ZARz0pjm5YM/maxresdefault.jpg",
        publishDate = Instant.parse("2021-11-01T00:00:00.000Z"),
        type = "Video 📺",
        topics = listOf(
            Topic(
                id = "1",
                name = "UI",
                shortDescription = "",
                longDescription = "long description",
                url = "URL",
                imageUrl = "image URL",
            ),
        ),
    ),
    NewsResource(
        id = "3",
        title = "Community tip on Paging",
        content = "Tips for using the Paging library from the developer community",
        url = "https://youtu.be/r5JgIyS3t3s",
        headerImageUrl = "https://i.ytimg.com/vi/r5JgIyS3t3s/maxresdefault.jpg",
        publishDate = Instant.parse("2021-11-08T00:00:00.000Z"),
        type = "Video 📺",
        topics = listOf(
            Topic(
                id = "1",
                name = "UI",
                shortDescription = "",
                longDescription = "long description",
                url = "URL",
                imageUrl = "image URL",
            ),
        ),
    ),
)
