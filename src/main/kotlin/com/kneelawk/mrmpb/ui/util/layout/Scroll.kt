package com.kneelawk.mrmpb.ui.util.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ScrollbarAdapter
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun VerticalScrollWrapper(
    modifier: Modifier, backgroundColor: Color = MaterialTheme.colors.surface,
    backgroundShape: Shape = MaterialTheme.shapes.medium, scrollbarPadding: Dp = 15.dp, adapter: ScrollbarAdapter,
    content: @Composable RowScope.() -> Unit
) {
    Row(modifier = modifier.background(backgroundColor, backgroundShape).clip(backgroundShape)) {
        content()

        var containerSize by remember { mutableStateOf(0) }
        val measure = remember { measureHeight { containerSize = it } }
        val isScrollable by remember { derivedStateOf { adapter.maxScrollOffset(containerSize) > 0f } }

        Layout({
            AnimatedVisibility(visible = isScrollable) {
                VerticalScrollbar(
                    adapter = adapter,
                    modifier = Modifier.fillMaxHeight().padding(end = scrollbarPadding)
                )
            }
        }, Modifier.fillMaxHeight(), measure)
    }
}

private fun measureHeight(setContainerSize: (Int) -> Unit) = MeasurePolicy { measurables, constraints ->
    setContainerSize(constraints.maxHeight)
    val placeable = measurables.firstOrNull()?.measure(constraints)
    layout(placeable?.width ?: 0, constraints.maxHeight) {
        placeable?.place(0, 0)
    }
}
