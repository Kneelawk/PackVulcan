package com.kneelawk.mrmpb.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kneelawk.mrmpb.ui.util.layout.AppContainerBox

@Composable
fun ModpackView(component: ModpackComponent) {
    AppContainerBox(component.modpackName) {
        OpenModpackDetailsView(component)
    }
}

@Composable
fun OpenModpackDetailsView(component: ModpackComponent) {
    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ModpackDetailsView(
            component.modpackLocation, null,
            component.modpackName, { component.modpackName = it },
            component.modpackAuthor, { component.modpackAuthor = it },
            component.modpackVersion, { component.modpackVersion = it }, false,
            component.minecraftVersion, { component.minecraftVersion = it }, false,
            component.loaderVersion, { component.loaderVersion = it }, false, enabled = !component.loading
        )
    }
}
