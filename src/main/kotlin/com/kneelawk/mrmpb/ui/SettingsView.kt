package com.kneelawk.mrmpb.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import com.kneelawk.mrmpb.GlobalSettings
import com.kneelawk.mrmpb.ui.theme.MrMpBTheme
import com.kneelawk.mrmpb.ui.util.layout.DialogContainerBox

@Composable
fun SettingsWindow(windowState: WindowState, onCloseRequest: () -> Unit) {
    Window(
        onCloseRequest = onCloseRequest, title = "Modrinth Modpack Builder",
        state = windowState
    ) {
        MrMpBTheme(GlobalSettings.darkMode) {
            SettingsView()
        }
    }
}

@Composable
fun SettingsView() {
    DialogContainerBox {
        Column(
            verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(IntrinsicSize.Max).align(Alignment.Center)
        ) {
            Text("Settings", style = MaterialTheme.typography.h3, color = MrMpBTheme.colors.headingColor)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dark Theme")
                Switch(
                    GlobalSettings.darkMode, onCheckedChange = { GlobalSettings.darkMode = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colors.primary,
                        checkedTrackColor = MaterialTheme.colors.secondary,
                        checkedTrackAlpha = 0.8F
                    )
                )
            }
        }
    }
}
