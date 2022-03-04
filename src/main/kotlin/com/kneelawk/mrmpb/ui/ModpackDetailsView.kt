package com.kneelawk.mrmpb.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kneelawk.mrmpb.ui.util.Form
import com.kneelawk.mrmpb.ui.util.OpenDirectoryDialog
import kotlin.io.path.pathString

@Composable
fun ModpackDetailsView(projectLocation: String, projectLocationChange: (String) -> Unit) {
    var projectLocationDialog by remember { mutableStateOf(false) }

    if (projectLocationDialog) {
        OpenDirectoryDialog("Select a project location...") { selection ->
            projectLocationDialog = false
            selection?.let { projectLocationChange(it.pathString) }
        }
    }

    Form(
        rowArrangement = Arrangement.spacedBy(10.dp, Alignment.Top), columnSpacing = 10.dp
    ) {
        Text("File", modifier = Modifier.formSection().padding(top = 10.dp), style = MaterialTheme.typography.h5)

        Text("Project Location:", modifier = Modifier.formLabel())
        TextField(projectLocation, projectLocationChange, modifier = Modifier.formField())
        Button(onClick = { projectLocationDialog = true }, modifier = Modifier.formConfigure()) {
            Text("...")
        }

        Text("Details", modifier = Modifier.formSection().padding(top = 10.dp), style = MaterialTheme.typography.h5)

        Text("Modpack Name:", modifier = Modifier.formLabel())
        TextField("", {}, modifier = Modifier.formField())

        Text("Modpack Author:", modifier = Modifier.formLabel())
        TextField("", {}, modifier = Modifier.formField())

        Text("Modpack Version:", modifier = Modifier.formLabel())
        TextField("0.0.1", {}, modifier = Modifier.formField())

        Text("Versions", modifier = Modifier.formSection().padding(top = 10.dp), style = MaterialTheme.typography.h5)

        Text("Minecraft Version:", modifier = Modifier.formLabel())
        TextField("1.18.1", {}, modifier = Modifier.formField())
        Button(onClick = {}, modifier = Modifier.formConfigure()) {
            Text("...")
        }

        Text("Loader Version:", modifier = Modifier.formLabel())
        TextField("Fabric 0.10.2", {}, modifier = Modifier.formField())
        Button(onClick = {}, modifier = Modifier.formConfigure()) {
            Text("...")
        }
    }
}
