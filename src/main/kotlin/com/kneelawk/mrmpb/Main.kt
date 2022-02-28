package com.kneelawk.mrmpb

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.kneelawk.mrmpb.ui.AppScreen
import com.kneelawk.mrmpb.ui.GlobalSettings
import com.kneelawk.mrmpb.ui.theme.MrMpBTheme

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication, title = "Modrinth Modpack Builder",
        state = WindowState(size = DpSize(1280.dp, 800.dp))
    ) {
        App()
    }
}

@Composable
@Preview
fun App() {
    MrMpBTheme(GlobalSettings.darkMode) {
        AppScreen()
    }
}
