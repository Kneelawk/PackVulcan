package com.kneelawk.mrmpb.ui.util

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

@Composable
fun ContainerBox(content: @Composable BoxScope.() -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        val contentColor by animateColorAsState(MaterialTheme.colors.onBackground)
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            content()
        }
    }
}
