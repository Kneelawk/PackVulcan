package com.kneelawk.mrmpb.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry

class ApplicationInstance {
    private val lifecycle = LifecycleRegistry()
    private val root = RootComponent(DefaultComponentContext(lifecycle))

    @OptIn(ExperimentalDecomposeApi::class)
    @Composable
    fun compose(onCloseRequest: () -> Unit) {
        val windowState = rememberWindowState(size = DpSize(1280.dp, 800.dp))

        LifecycleController(lifecycle, windowState)

        RootView(windowState, root, onCloseRequest)
    }
}