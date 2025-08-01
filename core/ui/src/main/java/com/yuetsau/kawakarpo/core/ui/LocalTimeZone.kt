/*
 * Copyright 2024 The Android Open Source Project
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

package com.yuetsau.kawakarpo.core.ui

import androidx.compose.runtime.compositionLocalOf
import kotlinx.datetime.TimeZone

/**
 * TimeZone that can be provided with the TimeZoneMonitor.
 * This way, it's not needed to pass every single composable the time zone to show in UI.
 */
val LocalTimeZone = compositionLocalOf { TimeZone.currentSystemDefault() }
