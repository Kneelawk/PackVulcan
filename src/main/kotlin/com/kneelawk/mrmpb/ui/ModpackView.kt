package com.kneelawk.mrmpb.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.kneelawk.mrmpb.ui.keyboard.KeyboardTracker
import com.kneelawk.mrmpb.ui.keyboard.shortcut
import com.kneelawk.mrmpb.ui.keyboard.shortcuts
import com.kneelawk.mrmpb.ui.theme.MrMpBIcons
import com.kneelawk.mrmpb.ui.theme.MrMpBTheme
import com.kneelawk.mrmpb.ui.util.layout.AppContainerBox
import com.kneelawk.mrmpb.ui.util.widgets.ListButton

@OptIn(ExperimentalComposeUiApi::class)
private val SAVE_SHORTCUT = shortcut().cmd().key(Key.S).finish()

@Composable
fun ModpackView(component: ModpackComponent, tracker: KeyboardTracker) {
    shortcuts(tracker, SAVE_SHORTCUT to {
        component.save()
    })

    AppContainerBox(
        title = component.modpackName,
        extraDrawerContent = {
            ListButton(
                onClick = { component.save() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(MrMpBIcons.save, "save")
                Text("Save", modifier = Modifier.padding(start = 10.dp))
                Box(Modifier.weight(1f))
                Text(SAVE_SHORTCUT.toString())
            }
        }
    ) {
        OpenModpackDetailsView(component)
    }
}

@Composable
fun OpenModpackDetailsView(component: ModpackComponent) {
    val uriHandler = LocalUriHandler.current

    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ModpackDetailsView(
            component.modpackLocation.toString(), null,
            component.modpackName, { component.modpackName = it },
            component.modpackAuthor, { component.modpackAuthor = it },
            component.editModpackVersion, { component.updateModpackVersion(it) }, component.modpackVersionError,
            component.editMinecraftVersion, { component.updateMinecraftVersion(it) },
            component.minecraftVersionError.isNotBlank(),
            component.editLoaderVersion, { component.updateLoaderVersion(it) },
            component.loaderVersionError.isNotBlank(), enabled = !component.loading
        )

        if (component.showLoadingIcon) {
            CircularProgressIndicator()
        }

        if (component.modpackVersionError) {
            val errorText = buildAnnotatedString {
                append("Modpack versions must comply with the ")

                pushStringAnnotation(tag = "URL", annotation = "https://semver.org/")
                withStyle(style = SpanStyle(color = MrMpBTheme.colors.linkColor)) {
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
}
