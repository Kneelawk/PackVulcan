package com.kneelawk.mrmpb.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kneelawk.mrmpb.ui.util.layout.ContainerBox
import com.kneelawk.mrmpb.ui.util.widgets.SmallButton

@Composable
fun CreateNewView(component: CreateNewComponent) {
    ContainerBox {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ModpackDetailsView(
                component.location, { component.location = it },
                component.name, { component.name = it },
                component.author, { component.author = it },
                component.version, { component.version = it }, component.versionError,
                component.editMinecraftVersion, { component.setMinecraftVersion(it) }, component.minecraftVersionError,
                component.editLoaderVersion, { component.setLoaderVersion(it) }, component.loaderVersionError
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End), verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                SmallButton(
                    onClick = { component.cancel() },
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
                ) {
                    Text("Cancel")
                }
                SmallButton(onClick = { component.create() }, enabled = component.createEnabled) {
                    Text("Create Modpack")
                }
            }
        }
    }
}
