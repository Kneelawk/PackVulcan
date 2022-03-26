package com.kneelawk.mrmpb.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kneelawk.mrmpb.ui.theme.MrMpBTheme
import com.kneelawk.mrmpb.ui.util.dialog.OpenDirectoryDialog
import com.kneelawk.mrmpb.ui.util.layout.Form
import com.kneelawk.mrmpb.ui.util.widgets.SmallButton
import com.kneelawk.mrmpb.ui.util.widgets.SmallTextField
import java.nio.file.Paths
import kotlin.io.path.pathString

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ModpackDetailsView(
    location: String, locationChange: ((String) -> Unit)?, name: String, nameChange: (String) -> Unit, author: String,
    authorChange: (String) -> Unit, version: String, versionChange: (String) -> Unit, versionError: Boolean,
    minecraftVersion: String, minecraftVersionChange: (String) -> Unit, minecraftVersionError: Boolean,
    loaderVersion: String, loaderVersionChange: (String) -> Unit, loaderVersionError: Boolean
) {
    val projectLocationEditable = locationChange != null
    var projectLocationDialog by remember { mutableStateOf(false) }

    if (projectLocationDialog && projectLocationEditable) {
        val initialFolder = if (location.isBlank()) {
            Paths.get(System.getProperty("user.home"))
        } else {
            Paths.get(location).normalize().parent ?: Paths.get(System.getProperty("user.home"))
        }
        OpenDirectoryDialog(
            title = "Select a project location...", initialFolder = initialFolder, initialSelection = location
        ) { selection ->
            projectLocationDialog = false
            selection?.let { locationChange!!(it.pathString) }
        }
    }

    Form(
        rowArrangement = Arrangement.spacedBy(10.dp, Alignment.Top), columnSpacing = 10.dp
    ) {
        Text(
            "File", modifier = Modifier.formSection(), style = MaterialTheme.typography.h5,
            color = MrMpBTheme.colors.headingColor
        )

        Text("Project Location:", modifier = Modifier.formLabel())
        SmallTextField(location, locationChange ?: {}, modifier = Modifier.formField())
        if (projectLocationEditable) {
            SmallButton(onClick = { projectLocationDialog = true }, modifier = Modifier.formConfigure()) {
                Text("...")
            }
        }

        Text(
            "Details", modifier = Modifier.formSection().padding(top = 10.dp), style = MaterialTheme.typography.h5,
            color = MrMpBTheme.colors.headingColor
        )

        Text("Modpack Name:", modifier = Modifier.formLabel())
        SmallTextField(name, nameChange, modifier = Modifier.formField())

        Text("Modpack Author:", modifier = Modifier.formLabel())
        SmallTextField(author, authorChange, modifier = Modifier.formField())

        Text("Modpack Version:", modifier = Modifier.formLabel())
        SmallTextField(
            version, versionChange, modifier = Modifier.formField(), isError = versionError
        )

        Text(
            "Versions", modifier = Modifier.formSection().padding(top = 10.dp), style = MaterialTheme.typography.h5,
            color = MrMpBTheme.colors.headingColor
        )

        Text("Minecraft Version:", modifier = Modifier.formLabel())
        SmallTextField(
            minecraftVersion, minecraftVersionChange, modifier = Modifier.formField(), isError = minecraftVersionError
        )
        SmallButton(onClick = {}, modifier = Modifier.formConfigure()) {
            Text("...")
        }
        // TODO: Minecraft version selector

        Text("Loader Version:", modifier = Modifier.formLabel())
        SmallTextField(
            loaderVersion, loaderVersionChange, modifier = Modifier.formField(), isError = loaderVersionError
        )
        SmallButton(onClick = {}, modifier = Modifier.formConfigure()) {
            Text("...")
        }
        // TODO: Loader version selector
    }
}
