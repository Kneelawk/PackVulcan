package com.kneelawk.packvulcan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kneelawk.packvulcan.engine.packwiz.PackwizProject
import com.kneelawk.packvulcan.ui.theme.PackVulcanTheme
import com.kneelawk.packvulcan.ui.util.dialog.file.OpenFileDialog
import com.kneelawk.packvulcan.ui.util.layout.AppContainerBox
import com.kneelawk.packvulcan.ui.util.widgets.SmallButton
import java.nio.file.Path
import kotlin.io.path.isDirectory

const val WELCOME_TEXT = "Modpack Builder"

@Composable
fun StartView(createNew: () -> Unit, openExisting: (Path) -> Unit) {
    var openProjectDialog by remember { mutableStateOf(false) }

    if (openProjectDialog) {
        OpenFileDialog(
            title = "Open Packwiz 'pack.toml'",
            chooserFilter = { it.isDirectory() || PackwizProject.isPackFile(it) }
        ) { selected ->
            openProjectDialog = false
            selected?.let { openExisting(it) }
        }
    }

    AppContainerBox("Welcome") {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(IntrinsicSize.Max).align(Alignment.Center)
            ) {
                Text(WELCOME_TEXT, style = MaterialTheme.typography.h2, color = PackVulcanTheme.colors.headingColor)
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