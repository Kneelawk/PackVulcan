package com.kneelawk.mrmpb.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.*
import com.arkivanov.decompose.extensions.compose.jetbrains.Children

@Composable
@Preview
fun RootView(component: RootComponent) {
    var openProjectDialog by remember { mutableStateOf(false) }

    if (openProjectDialog) {
        OpenFileDialog("Open Packwiz 'pack.toml'") { selected ->
            openProjectDialog = false
            selected?.let { component.openModpack(it) }
        }
    }

    Children(component.routerState) {
        when (val instance = it.instance) {
            CurrentScreen.Start -> StartView(
                createNew = { component.openCreateNew() }, openExisting = { openProjectDialog = true },
                openSettings = { component.openSettings() })
            CurrentScreen.Settings -> SettingsView(finish = { component.goBack() })
            is CurrentScreen.CreateNew -> CreateNewView(instance.component)
            is CurrentScreen.Modpack -> ModpackView(instance.component)
        }
    }
}