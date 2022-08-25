package com.kneelawk.packvulcan.ui.util.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.SplitPaneScope
import java.awt.Cursor

@OptIn(ExperimentalSplitPaneApi::class)
fun SplitPaneScope.styledSplitter() {
    splitter {
        visiblePart {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxHeight()) {
                Box(
                    Modifier
                        .width(3.dp)
                        .height(30.dp)
                        .background(MaterialTheme.colors.surface, shape = MaterialTheme.shapes.small)
                )
            }
        }
        handle {
            Box(
                Modifier
                    .markAsHandle()
                    .pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))
                    .width(10.dp)
                    .fillMaxHeight()
            )
        }
    }
}
