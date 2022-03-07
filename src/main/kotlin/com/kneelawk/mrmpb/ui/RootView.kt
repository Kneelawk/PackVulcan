package com.kneelawk.mrmpb.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.animation.child.crossfade
import com.kneelawk.mrmpb.ui.util.dialog.OpenFileDialog

@OptIn(ExperimentalDecomposeApi::class)
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

    val backgroundColor by animateColorAsState(MaterialTheme.colors.background)
    Children(
        component.routerState, animation = crossfade(), modifier = Modifier.background(backgroundColor)
    ) {
        when (val instance = it.instance) {
            CurrentScreen.Start -> StartView(
                createNew = { component.openCreateNew() }, openExisting = { openProjectDialog = true },
                openSettings = { component.openSettings() })
            CurrentScreen.Settings -> SettingsView(finish = { component.goBack() })
            is CurrentScreen.CreateNew -> CreateNewView(component, instance.component)
            is CurrentScreen.Modpack -> ModpackView(instance.component)
        }
    }
}
