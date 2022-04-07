package com.kneelawk.packvulcan.ui.instance

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import com.kneelawk.packvulcan.ui.SettingsWindow

object SettingsInstance : AbstractInstance(DpSize(1280.dp, 720.dp)) {
    @Composable
    override fun compose(windowState: WindowState, onCloseRequest: () -> Unit) {
        SettingsWindow(windowState, onCloseRequest)
    }
}