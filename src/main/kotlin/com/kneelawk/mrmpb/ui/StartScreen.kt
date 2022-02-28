package com.kneelawk.mrmpb.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

const val WELCOME_TEXT = "Modpack Builder"

@Composable
fun StartScreen(createNew: () -> Unit, openExisting: () -> Unit, openSettings: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background)
    ) {
        Column(
            verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(IntrinsicSize.Max).align(Alignment.Center)
        ) {
            Text(WELCOME_TEXT, style = MaterialTheme.typography.h2, color = MaterialTheme.colors.onBackground)
            Button(onClick = createNew, modifier = Modifier.fillMaxWidth()) {
                Text("Create New Project")
            }
            Button(onClick = openExisting, modifier = Modifier.fillMaxWidth()) {
                Text("Open Existing Project")
            }
        }

        IconButton(onClick = openSettings, modifier = Modifier.align(Alignment.TopEnd)) {
            Icon(Icons.Default.Settings, "Settings")
        }
    }
}