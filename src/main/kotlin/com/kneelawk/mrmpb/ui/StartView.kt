package com.kneelawk.mrmpb.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kneelawk.mrmpb.ui.theme.MrMpBTheme
import com.kneelawk.mrmpb.ui.util.layout.ContainerBox
import com.kneelawk.mrmpb.ui.util.widgets.SmallButton

const val WELCOME_TEXT = "Modpack Builder"

@Composable
fun StartView(createNew: () -> Unit, openExisting: () -> Unit, openSettings: () -> Unit) {
    ContainerBox {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(IntrinsicSize.Max).align(Alignment.Center)
        ) {
            Text(WELCOME_TEXT, style = MaterialTheme.typography.h2, color = MrMpBTheme.colors.headingColor)
            SmallButton(onClick = createNew, modifier = Modifier.fillMaxWidth()) {
                Text("Create New Project")
            }
            SmallButton(onClick = openExisting, modifier = Modifier.fillMaxWidth()) {
                Text("Open Existing Project")
            }
        }

        IconButton(onClick = openSettings, modifier = Modifier.align(Alignment.TopEnd)) {
            Icon(Icons.Default.Settings, "Settings")
        }
    }
}