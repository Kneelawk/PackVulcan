package com.kneelawk.packvulcan.ui.modrinth.install

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LoadingView(
    loading: Boolean, loadingText: String, modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colors.surface
) {
    val loadingStates = remember { MutableTransitionState(true) }
    loadingStates.targetState = loading

    if (loadingStates.currentState || loadingStates.targetState) {
        val transition = updateTransition(loadingStates, "LoadingView")
        val alpha by transition.animateFloat(transitionSpec = {
            if (true isTransitioningTo false) {
                tween(loadingEndDuration)
            } else {
                tween(loadingRestartDuration)
            }
        }) {
            if (it) 1f else 0f
        }

        Box(modifier = modifier.background(background.copy(alpha = alpha)), contentAlignment = Alignment.Center) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator()

                Text(loadingText)
            }
        }
    }
}

private val loadingEndDuration = 120
private val loadingRestartDuration = 75
