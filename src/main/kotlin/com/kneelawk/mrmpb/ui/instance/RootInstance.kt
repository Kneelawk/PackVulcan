package com.kneelawk.mrmpb.ui.instance

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.kneelawk.mrmpb.ui.RootComponent
import com.kneelawk.mrmpb.ui.RootView

class RootInstance : AbstractInstance(DpSize(1280.dp, 800.dp)) {
    private val root = RootComponent(DefaultComponentContext(lifecycle))

    @Composable
    override fun compose(windowState: WindowState, onCloseRequest: () -> Unit) {
        RootView(windowState, root, onCloseRequest)
    }
}