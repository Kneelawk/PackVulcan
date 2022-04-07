package com.kneelawk.packvulcan

import androidx.compose.ui.window.application
import com.kneelawk.packvulcan.net.shutdownHttpClient
import com.kneelawk.packvulcan.ui.instance.InstanceManager
import com.kneelawk.packvulcan.ui.util.initSwing
import com.kneelawk.packvulcan.util.ApplicationScope

fun main() {
    // Must be the very first thing in the application, so that Swing components have not selected a LAF yet.
    initSwing()

    // Load the global settings
    GlobalSettings.load()

    InstanceManager.newRoot()

    application(exitProcessOnExit = false) {
        InstanceManager.compose(::exitApplication)
    }

    // Shutdown http client
    shutdownHttpClient()

    // Store global settings
    GlobalSettings.store()

    // Shutdown the application coroutine scope
    ApplicationScope.shutdown()
}
