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

package com.yuetsau.kawakarpo.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import com.yuetsau.kawakarpo.PACKAGE_NAME
import com.yuetsau.kawakarpo.foryou.forYouScrollFeedDownUp
import com.yuetsau.kawakarpo.foryou.forYouSelectTopics
import com.yuetsau.kawakarpo.foryou.forYouWaitForContent
import com.yuetsau.kawakarpo.startActivityAndAllowNotifications
import org.junit.Rule
import org.junit.Test

/**
 * Baseline Profile of the "For You" screen
 */
class ForYouBaselineProfile {
    @get:Rule val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() =
        baselineProfileRule.collect(PACKAGE_NAME) {
            startActivityAndAllowNotifications()

            // Scroll the feed critical user journey
            forYouWaitForContent()
            forYouSelectTopics(true)
            forYouScrollFeedDownUp()
        }
}
