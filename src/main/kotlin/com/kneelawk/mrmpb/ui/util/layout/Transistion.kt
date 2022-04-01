package com.kneelawk.mrmpb.ui.util.layout

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
