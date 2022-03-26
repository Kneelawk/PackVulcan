package com.kneelawk.mrmpb

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.kneelawk.mrmpb.ui.RootComponent
import com.kneelawk.mrmpb.ui.RootView
import com.kneelawk.mrmpb.ui.theme.MrMpBTheme
import com.kneelawk.mrmpb.ui.util.initSwing

@OptIn(ExperimentalDecomposeApi::class)
fun main() {
    // Must be the very first thing in the application, so that Swing components have not selected a LAF yet.
    initSwing()

    // Load the global settings
    GlobalSettings.load()

    val lifecycle = LifecycleRegistry()
    val root = RootComponent(DefaultComponentContext(lifecycle))

    application(exitProcessOnExit = false) {
        val windowState = rememberWindowState(size = DpSize(1280.dp, 800.dp))

        LifecycleController(lifecycle, windowState)

        Window(
            onCloseRequest = ::exitApplication, title = "Modrinth Modpack Builder",
            state = windowState
        ) {
            MrMpBTheme(GlobalSettings.darkMode) {
                RootView(root)
            }
        }
    }

    // Store global settings
    GlobalSettings.store()
}
