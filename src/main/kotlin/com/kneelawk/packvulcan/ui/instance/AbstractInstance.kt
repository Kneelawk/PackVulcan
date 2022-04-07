package com.kneelawk.packvulcan.ui.instance

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry

abstract class AbstractInstance(private val size: DpSize) : Instance {
    protected val lifecycle = LifecycleRegistry()

    @Composable
    protected abstract fun compose(windowState: WindowState, onCloseRequest: () -> Unit)

    @OptIn(ExperimentalDecomposeApi::class)
    @Composable
    override fun compose(onCloseRequest: () -> Unit) {
        val windowState = rememberWindowState(size = size)

        LifecycleController(lifecycle, windowState)

        compose(windowState, onCloseRequest)
    }
}