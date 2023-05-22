package com.kneelawk.packvulcan.ui.detail

import androidx.compose.runtime.*
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.kneelawk.packvulcan.GlobalSettings
import com.kneelawk.packvulcan.ui.theme.PackVulcanTheme
import com.kneelawk.packvulcan.ui.util.layout.DialogContainerBox

@Composable
fun DetailWindow(onCloseRequest: () -> Unit, selector: DetailSelector) {
    val state = rememberWindowState(size = DpSize(1280.dp, 800.dp))

    var title by remember { mutableStateOf("Loading...") }

    Window(onCloseRequest = onCloseRequest, state = state, title = title) {
        PackVulcanTheme(GlobalSettings.darkMode) {
            DialogContainerBox {
                DetailView(rememberDetailController(selector = selector, updateTitle = { title = it }))
            }
        }
    }
}

@Composable
fun DetailView(controller: DetailInterface) {

}
