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
plugins {
    alias(libs.plugins.kawakarpo.android.library)
    alias(libs.plugins.kawakarpo.hilt)
}

android {
    namespace = "com.yuetsau.kawakarpo.core.data.test"
}

dependencies {
    api(projects.core.data)

    implementation(libs.hilt.android.testing)
    implementation(libs.androidx.test.ext)
}
