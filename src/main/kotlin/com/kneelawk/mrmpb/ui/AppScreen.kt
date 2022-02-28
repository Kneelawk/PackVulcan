package com.kneelawk.mrmpb.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.*

@Composable
@Preview
fun AppScreen() {
    var screen: CurrentScreen by remember { mutableStateOf(CurrentScreen.Start) }
    var openProjectDialog by remember { mutableStateOf(false) }

    if (openProjectDialog) {
        OpenFileDialog("Open Packwiz 'pack.toml'") { selected ->
            openProjectDialog = false
            selected?.let { screen = CurrentScreen.OpenExisting(it) }
        }
    }

    when (screen) {
        CurrentScreen.Start -> StartScreen(
            createNew = { screen = CurrentScreen.CreateNew }, openExisting = { openProjectDialog = true },
            openSettings = {})
        CurrentScreen.CreateNew -> {}
        is CurrentScreen.OpenExisting -> {}
    }
}
