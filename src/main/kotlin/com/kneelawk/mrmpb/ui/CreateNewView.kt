package com.kneelawk.mrmpb.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kneelawk.mrmpb.ui.util.ContainerBox
import com.kneelawk.mrmpb.ui.util.Form

@Composable
fun CreateNewView(root: RootComponent, component: CreateNewComponent) {
    ContainerBox {
        Column(modifier = Modifier.padding(20.dp)) {
            Form(
                rowArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
                columnSpacing = 10.dp
            ) {
                Text("File", modifier = Modifier.formSection(), style = MaterialTheme.typography.h5)

                Text("Modpack Location:", modifier = Modifier.formLabel())
                TextField("", {}, modifier = Modifier.formField())
                Button(onClick = {}, modifier = Modifier.formConfigure()) {
                    Text("...")
                }

                Text("Details", modifier = Modifier.formSection(), style = MaterialTheme.typography.h5)

                Text("Modpack Name:", modifier = Modifier.formLabel())
                TextField("", {}, modifier = Modifier.formField())

                Text("Modpack Author:", modifier = Modifier.formLabel())
                TextField("", {}, modifier = Modifier.formField())

                Text("Modpack Version:", modifier = Modifier.formLabel())
                TextField("0.0.1", {}, modifier = Modifier.formField())

                Text("Versions", modifier = Modifier.formSection(), style = MaterialTheme.typography.h5)

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
