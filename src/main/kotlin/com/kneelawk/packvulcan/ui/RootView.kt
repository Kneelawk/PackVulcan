package com.kneelawk.packvulcan.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.animation.child.childAnimation
import com.arkivanov.decompose.extensions.compose.jetbrains.animation.child.fade
import com.kneelawk.packvulcan.GlobalSettings
import com.kneelawk.packvulcan.ui.keyboard.rememberKeyboardTracker
import com.kneelawk.packvulcan.ui.theme.PackVulcanTheme

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun RootView(windowState: WindowState, component: RootComponent, onCloseRequest: () -> Unit) {
    val tracker = rememberKeyboardTracker()

    Window(
        onCloseRequest = onCloseRequest, title = "PackVulcan",
        state = windowState, onKeyEvent = tracker::keyPressed
    ) {
        PackVulcanTheme(GlobalSettings.darkMode) {
            val backgroundColor by animateColorAsState(MaterialTheme.colors.background)
            Children(
                component.routerState, animation = childAnimation(fade()),
                modifier = Modifier.background(backgroundColor)
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
