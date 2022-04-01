package com.kneelawk.mrmpb.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kneelawk.mrmpb.ui.theme.MrMpBTheme
import com.kneelawk.mrmpb.ui.util.dialog.file.OpenFileDialog
import com.kneelawk.mrmpb.ui.util.layout.AppContainerBox
import com.kneelawk.mrmpb.ui.util.widgets.SmallButton
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.name

const val WELCOME_TEXT = "Modpack Builder"

@Composable
fun StartView(createNew: () -> Unit, openExisting: (Path) -> Unit) {
    var openProjectDialog by remember { mutableStateOf(false) }

    if (openProjectDialog) {
        OpenFileDialog(
            title = "Open Packwiz 'pack.toml'",
            chooserFilter = { it.isDirectory() || it.name == "pack.toml" }
        ) { selected ->
            openProjectDialog = false
            selected?.let { openExisting(it) }
        }
    }

    AppContainerBox("Modpack Builder") {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(IntrinsicSize.Max).align(Alignment.Center)
            ) {
                Text(WELCOME_TEXT, style = MaterialTheme.typography.h2, color = MrMpBTheme.colors.headingColor)
                SmallButton(onClick = createNew, modifier = Modifier.fillMaxWidth()) {
                    Text("Create New Project")
                }
                SmallButton(onClick = { openProjectDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Open Existing Project")
                }
            }
        }
    }
}