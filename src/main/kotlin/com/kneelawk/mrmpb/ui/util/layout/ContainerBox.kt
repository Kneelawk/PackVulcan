package com.kneelawk.mrmpb.ui.util.layout

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kneelawk.mrmpb.ui.instance.InstanceManager
import com.kneelawk.mrmpb.ui.util.FixedWidthRectangleShape
import com.kneelawk.mrmpb.ui.util.widgets.ListButton
import kotlinx.coroutines.launch

@Composable
fun AppContainerBox(
    title: String, extraDrawerContent: @Composable ColumnScope.() -> Unit = {}, content: @Composable () -> Unit
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    AppContainerBox(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = {
                    Text(title)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        scope.launch {
                            scaffoldState.drawerState.open()
                        }
                    }) {
                        Icon(Icons.Default.Menu, "menu")
                    }
                }
            )
        },
        drawerContent = {
            TopAppBar(
                title = {
                    Text("Menu")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        scope.launch {
                            scaffoldState.drawerState.close()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, "close menu")
                    }
                }
            )

            ListButton(onClick = {
                InstanceManager.openSettings()
            }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Settings, "settings")
                Text("Settings", Modifier.padding(start = 5.dp))
            }

            Divider()

            extraDrawerContent()
        },
        content = content
    )
}

@Composable
fun AppContainerBox(
    scaffoldState: ScaffoldState, topBar: @Composable () -> Unit, drawerContent: @Composable ColumnScope.() -> Unit,
    content: @Composable () -> Unit
) {
    val backgroundColor by animateColorAsState(MaterialTheme.colors.background)
    val drawerBackgroundColor by animateColorAsState(MaterialTheme.colors.surface)
    Scaffold(
        scaffoldState = scaffoldState, topBar = topBar, backgroundColor = backgroundColor,
        drawerContent = drawerContent, drawerBackgroundColor = drawerBackgroundColor,
        drawerShape = FixedWidthRectangleShape(400f)
    ) {
        val contentColor by animateColorAsState(MaterialTheme.colors.onBackground)
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            content()
        }
    }
}

@Composable
fun DialogContainerBox(content: @Composable BoxScope.() -> Unit) {
    val backgroundColor by animateColorAsState(MaterialTheme.colors.background)
    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        val contentColor by animateColorAsState(MaterialTheme.colors.onBackground)
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            content()
        }
    }
}
