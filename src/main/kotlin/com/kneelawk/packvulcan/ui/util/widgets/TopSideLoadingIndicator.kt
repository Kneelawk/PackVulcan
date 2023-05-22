package com.kneelawk.packvulcan.ui.util.widgets

import androidx.compose.animation.*
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun BoxScope.TopSideLoadingIndicator(loading: Boolean) {
    AnimatedVisibility(
        visible = loading,
        modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        LinearProgressIndicator(Modifier.fillMaxWidth())
    }
}
