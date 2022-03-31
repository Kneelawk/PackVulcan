package com.kneelawk.mrmpb.ui.instance

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import com.kneelawk.mrmpb.ui.SettingsWindow

object SettingsInstance : AbstractInstance(DpSize(1280.dp, 720.dp)) {
    @Composable
    override fun compose(windowState: WindowState, onCloseRequest: () -> Unit) {
        SettingsWindow(windowState, onCloseRequest)
    }
}