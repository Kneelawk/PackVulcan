package com.kneelawk.mrmpb.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kneelawk.mrmpb.GlobalSettings
import com.kneelawk.mrmpb.ui.util.ContainerBox

@Composable
fun SettingsView(finish: () -> Unit) {
    ContainerBox {
        Column(
            verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(IntrinsicSize.Max).align(Alignment.Center)
        ) {
            Text("Settings", style = MaterialTheme.typography.h3)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dark Theme")
                Switch(
                    GlobalSettings.darkMode, onCheckedChange = { GlobalSettings.darkMode = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colors.primary,
                        checkedTrackColor = MaterialTheme.colors.secondary,
                        checkedTrackAlpha = 0.8F
                    )
                )
            }
        }

        IconButton(onClick = finish, modifier = Modifier.align(Alignment.TopStart)) {
            Icon(Icons.Default.ArrowBack, "Go Back")
        }
    }
}
