package com.kneelawk.packvulcan.ui.util.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ScrollbarAdapter
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.unit.dp

@Composable
fun VerticalScrollWrapper(
    adapter: ScrollbarAdapter, modifier: Modifier = Modifier, backgroundColor: Color = MaterialTheme.colors.surface,
    backgroundShape: Shape = MaterialTheme.shapes.medium, scrollbarPadding: PaddingValues = PaddingValues(end = 15.dp),
    enabled: Boolean = true, content: @Composable RowScope.() -> Unit
) {
    Row(modifier = modifier.background(backgroundColor, backgroundShape).clip(backgroundShape)) {
        content()

        var containerSize by remember { mutableStateOf(0) }
        val measure = remember { measureHeight { containerSize = it } }
        val isScrollable by remember { derivedStateOf { adapter.maxScrollOffset(containerSize) > 0f } }

        Layout({
            AnimatedVisibility(visible = isScrollable && enabled) {
                VerticalScrollbar(
                    adapter = adapter,
                    modifier = Modifier.fillMaxHeight().padding(scrollbarPadding),
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
