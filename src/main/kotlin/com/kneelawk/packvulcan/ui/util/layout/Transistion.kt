/*
 * Copyright 2019 The Android Open Source Project
 *
 * Modifications by Kneelawk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kneelawk.packvulcan.ui.util.layout

import androidx.compose.animation.*

@OptIn(ExperimentalAnimationApi::class)
fun <T : Comparable<T>> AnimatedContentScope<T>.slidingTransitionSpec(): ContentTransform {
    return if (targetState > initialState) {
        slideInHorizontally { width -> width } + fadeIn() with
                slideOutHorizontally { width -> -width } + fadeOut()
    } else {
        slideInHorizontally { width -> -width } + fadeIn() with
                slideOutHorizontally { width -> width } + fadeOut()
    }.using(
        SizeTransform(clip = false)
    )
}
