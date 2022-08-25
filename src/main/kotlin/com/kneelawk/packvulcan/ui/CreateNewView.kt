package com.kneelawk.packvulcan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.kneelawk.packvulcan.ui.theme.PackVulcanTheme
import com.kneelawk.packvulcan.ui.util.layout.AppContainerBox
import com.kneelawk.packvulcan.ui.util.widgets.SmallButton

const val CREATE_NEW_WINDOW_TITLE = "Create New Modpack"

@Composable
fun CreateNewView(component: CreateNewComponent, controls: WindowControls) {
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(Unit) {
        controls.title = CREATE_NEW_WINDOW_TITLE
    }

    AppContainerBox("Create New Modpack") {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ModpackDetailsView(
                component.location, { component.location = it },
                component.name, { component.name = it },
                component.author, { component.author = it },
                component.version, { component.version = it }, component.versionError,
                component.editMinecraftVersion, { component.setMinecraftVersion(it) },
                component.minecraftVersionError.isNotBlank(),
                component.editLoaderVersion, { component.setLoaderVersion(it) },
                component.loaderVersionError.isNotBlank()
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End), verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (component.showLoadingIcon) {
                    CircularProgressIndicator()
                }

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

            if (component.versionError) {
                val errorText = buildAnnotatedString {
                    append("Modpack versions must comply with the ")

                    pushStringAnnotation(tag = "URL", annotation = "https://semver.org/")
                    withStyle(style = SpanStyle(color = PackVulcanTheme.colors.linkColor)) {
                        append("SemVer v2 versioning scheme")
                    }
                    pop()

                    append(".")
                }
                ClickableText(errorText, style = TextStyle(color = MaterialTheme.colors.error)) { offset ->
                    errorText.getStringAnnotations("URL", offset, offset).firstOrNull()
                        ?.let { uriHandler.openUri(it.item) }
                }
            }

            if (component.minecraftVersionError.isNotBlank()) {
                Text(component.minecraftVersionError, style = TextStyle(color = MaterialTheme.colors.error))
            }

            if (component.loaderVersionError.isNotBlank()) {
                Text(component.loaderVersionError, style = TextStyle(color = MaterialTheme.colors.error))
            }
        }
    }
}
