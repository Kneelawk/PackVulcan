package com.kneelawk.mrmpb.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.animation.child.crossfade
import com.kneelawk.mrmpb.GlobalSettings
import com.kneelawk.mrmpb.ui.instance.InstanceManager
import com.kneelawk.mrmpb.ui.keyboard.rememberKeyboardTracker
import com.kneelawk.mrmpb.ui.theme.MrMpBTheme

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun RootView(windowState: WindowState, component: RootComponent, onCloseRequest: () -> Unit) {
    val tracker = rememberKeyboardTracker()

    Window(
        onCloseRequest = onCloseRequest, title = "Modrinth Modpack Builder",
        state = windowState, onKeyEvent = tracker::keyPressed
    ) {
        MrMpBTheme(GlobalSettings.darkMode) {
            val backgroundColor by animateColorAsState(MaterialTheme.colors.background)
            Children(
                component.routerState, animation = crossfade(), modifier = Modifier.background(backgroundColor)
            ) { child ->
                when (val instance = child.instance) {
                    CurrentScreen.Start -> StartView(
                        createNew = { component.openCreateNew() }, openExisting = { component.openModpack(it) })
                    is CurrentScreen.CreateNew -> CreateNewView(instance.component)
                    is CurrentScreen.Modpack -> ModpackView(instance.component, tracker)
                }
            }
        }
    }
}
