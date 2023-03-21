package com.kneelawk.packvulcan.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation
import com.kneelawk.packvulcan.GlobalSettings
import com.kneelawk.packvulcan.ui.keyboard.rememberKeyboardTracker
import com.kneelawk.packvulcan.ui.theme.PackVulcanTheme

const val DEFAULT_WINDOW_TITLE = "PackVulcan"

@Composable
fun RootView(windowState: WindowState, component: RootComponent, onCloseRequest: () -> Unit) {
    val tracker = rememberKeyboardTracker()

    Window(
        onCloseRequest = onCloseRequest, title = component.windowControls.title,
        state = windowState, onKeyEvent = tracker::keyPressed
    ) {
        PackVulcanTheme(GlobalSettings.darkMode) {
            val backgroundColor by animateColorAsState(MaterialTheme.colors.background)
            Children(
                component.childStack, animation = stackAnimation(fade()),
                modifier = Modifier.background(backgroundColor)
            ) { child ->
                when (val instance = child.instance) {
                    CurrentScreen.Start -> StartView(component.windowControls,
                        createNew = { component.openCreateNew() }) { component.openModpack(it) }

                    is CurrentScreen.CreateNew -> CreateNewView(instance.component, component.windowControls)
                    is CurrentScreen.Modpack -> ModpackView(instance.component, component.windowControls, tracker)
                }
            }
        }
    }
}
