package com.kneelawk.mrmpb

import androidx.compose.ui.window.application
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.kneelawk.mrmpb.net.shutdownHttpClient
import com.kneelawk.mrmpb.ui.ApplicationInstanceManager
import com.kneelawk.mrmpb.ui.util.initSwing

fun main() {
    // Must be the very first thing in the application, so that Swing components have not selected a LAF yet.
    initSwing()

    // Load the global settings
    GlobalSettings.load()

    ApplicationInstanceManager.openInstance()

    application(exitProcessOnExit = false) {
        ApplicationInstanceManager.compose(::exitApplication)
    }

    // Shutdown http client
    shutdownHttpClient()

    // Store global settings
    GlobalSettings.store()
}
