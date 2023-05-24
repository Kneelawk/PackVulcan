package com.kneelawk.packvulcan.ui.util.markdown

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

object MDThematicBreak : MDNode {
    @Composable
    override fun render() {
        Divider(Modifier.fillMaxWidth())
    }

    override fun toString(): String {
        return "MDThematicBreak"
    }
}

object MDHardLineBreak : MDNode {
    @Composable
    override fun render() {
        Box(Modifier.fillMaxWidth())
    }

    override fun toString(): String {
        return "MDHardLineBreak"
    }
}
