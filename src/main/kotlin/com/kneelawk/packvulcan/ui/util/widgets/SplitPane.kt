package com.kneelawk.packvulcan.ui.util.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
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
            Column(verticalArrangement = Arrangement.Center) {
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
