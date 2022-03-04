package com.kneelawk.mrmpb.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kneelawk.mrmpb.ui.util.ContainerBox

@Composable
fun CreateNewView(root: RootComponent, component: CreateNewComponent) {
    ContainerBox {
        Column(modifier = Modifier.padding(20.dp)) {
            var projectLocation by remember { mutableStateOf("") }

            ModpackDetailsView(projectLocation, { projectLocation = it })

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End), verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = { root.goBack() }) {
                    Text("Cancel")
                }
                Button(onClick = {}, enabled = false) {
                    Text("Create Modpack")
                }
            }
        }
    }
}
