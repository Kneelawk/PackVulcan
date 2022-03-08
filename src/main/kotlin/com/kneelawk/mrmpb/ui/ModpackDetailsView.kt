package com.kneelawk.mrmpb.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kneelawk.mrmpb.ui.theme.MrMpBTheme
import com.kneelawk.mrmpb.ui.util.Form
import com.kneelawk.mrmpb.ui.util.SmallButton
import com.kneelawk.mrmpb.ui.util.SmallTextField
import com.kneelawk.mrmpb.ui.util.dialog.OpenDirectoryDialog
import java.nio.file.Paths
import kotlin.io.path.pathString

@Composable
fun ModpackDetailsView(projectLocation: String, projectLocationChange: (String) -> Unit) {
    var projectLocationDialog by remember { mutableStateOf(false) }

    if (projectLocationDialog) {
        val initialFolder = if (projectLocation.isBlank()) {
            Paths.get(System.getProperty("user.home"))
        } else {
            Paths.get(projectLocation).normalize().parent ?: Paths.get(System.getProperty("user.home"))
        }
        OpenDirectoryDialog(
            title = "Select a project location...", initialFolder = initialFolder, initialSelection = projectLocation
        ) { selection ->
            projectLocationDialog = false
            selection?.let { projectLocationChange(it.pathString) }
        }
    }

    Form(
        rowArrangement = Arrangement.spacedBy(10.dp, Alignment.Top), columnSpacing = 10.dp
    ) {
        Text(
            "File", modifier = Modifier.formSection().padding(top = 10.dp), style = MaterialTheme.typography.h5,
            color = MrMpBTheme.colors.headingColor
        )

        Text("Project Location:", modifier = Modifier.formLabel())
        SmallTextField(projectLocation, projectLocationChange, modifier = Modifier.formField())
        SmallButton(onClick = { projectLocationDialog = true }, modifier = Modifier.formConfigure()) {
            Text("...")
        }

        Text(
            "Details", modifier = Modifier.formSection().padding(top = 10.dp), style = MaterialTheme.typography.h5,
            color = MrMpBTheme.colors.headingColor
        )

        Text("Modpack Name:", modifier = Modifier.formLabel())
        SmallTextField("", {}, modifier = Modifier.formField())

        Text("Modpack Author:", modifier = Modifier.formLabel())
        SmallTextField("", {}, modifier = Modifier.formField())

        Text("Modpack Version:", modifier = Modifier.formLabel())
        SmallTextField("0.0.1", {}, modifier = Modifier.formField())

        Text(
            "Versions", modifier = Modifier.formSection().padding(top = 10.dp), style = MaterialTheme.typography.h5,
            color = MrMpBTheme.colors.headingColor
        )

        Text("Minecraft Version:", modifier = Modifier.formLabel())
        SmallTextField("1.18.1", {}, modifier = Modifier.formField())
        SmallButton(onClick = {}, modifier = Modifier.formConfigure()) {
            Text("...")
        }

        Text("Loader Version:", modifier = Modifier.formLabel())
        SmallTextField("Fabric 0.10.2", {}, modifier = Modifier.formField())
        SmallButton(onClick = {}, modifier = Modifier.formConfigure()) {
            Text("...")
        }
    }
}
