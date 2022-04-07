package com.kneelawk.packvulcan.ui.instance

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.kneelawk.packvulcan.ui.RootComponent
import com.kneelawk.packvulcan.ui.RootView

class RootInstance : AbstractInstance(DpSize(1280.dp, 800.dp)) {
    private val root = RootComponent(DefaultComponentContext(lifecycle))

    @Composable
    override fun compose(windowState: WindowState, onCloseRequest: () -> Unit) {
        RootView(windowState, root, onCloseRequest)
    }
}