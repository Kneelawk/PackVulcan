package com.kneelawk.packvulcan.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.kneelawk.packvulcan.ui.theme.PackVulcanTheme

@Composable
fun ModpackCurrentDetailsView(component: ModpackComponent) {
    val uriHandler = LocalUriHandler.current

    Box {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ModpackDetailsView(
                location = component.modpackLocation.toString(),
                locationChange = null,
                name = component.modpackName,
                nameChange = { component.modpackName = it },
                author = component.modpackAuthor,
                authorChange = { component.modpackAuthor = it },
                version = component.editModpackVersion,
                versionChange = { component.updateModpackVersion(it) },
                versionError = component.modpackVersionError,
                minecraftVersion = component.editMinecraftVersion,
                minecraftVersionChange = { component.updateMinecraftVersion(it) },
                minecraftVersionError = component.minecraftVersionError.isNotBlank(),
                loaderVersion = component.editLoaderVersion,
                loaderVersionChange = { component.updateLoaderVersion(it) },
                loaderVersionError = component.loaderVersionError.isNotBlank(),
                additionalLoaders = component.additionalLoaders,
                loaderSelected = { component.extraAcceptableVersions.loaders.contains(it.packwizName) },
                toggleLoader = component::toggleAdditionalLoader,
                additionalMinecraftText = component.additionalMinecraftText,
                additionalMinecraftTextChange = { component.additionalMinecraftText = it },
                additionalMinecraftVersions = component.extraAcceptableVersions.minecraft.toList(),
                additionalMinecraftOptions = component.minecraftVersions,
                addAdditionalMinecraft = component::addAdditionalMinecraft,
                removeAdditionalMinecraft = component::removeAdditionalMinecraft,
                additionalMinecraftTextError = false,
                enabled = !component.loading
            )

            if (component.modpackVersionError) {
                val errorText = buildAnnotatedString {
                    append("Modpack versions must comply with the ")

                    pushStringAnnotation(tag = "URL", annotation = "https://semver.org/")
                    withStyle(style = SpanStyle(color = PackVulcanTheme.colors.linkColor)) {
                        append("SemVer v2 versioning scheme")
                    }
                    pop()

                    append(". The version '${component.modpackVersion}' will be used instead.")
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

        AnimatedVisibility(component.showLoadingIcon) {
            LinearProgressIndicator(modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth())
        }
    }
}
