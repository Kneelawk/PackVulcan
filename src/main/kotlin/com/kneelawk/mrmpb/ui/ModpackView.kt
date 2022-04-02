package com.kneelawk.mrmpb.ui

import androidx.compose.animation.*
import androidx.compose.foundation.ScrollbarAdapter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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
import com.kneelawk.mrmpb.ui.util.layout.VerticalScrollWrapper
import com.kneelawk.mrmpb.ui.util.layout.slidingTransitionSpec
import com.kneelawk.mrmpb.ui.util.widgets.ListButton

@OptIn(ExperimentalComposeUiApi::class)
private val SAVE_SHORTCUT = shortcut().cmd().key(Key.S).finish()

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ModpackView(component: ModpackComponent, tracker: KeyboardTracker) {
    var curTab by remember { mutableStateOf(ModpackTab.DETAILS) }

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
        Column {
            TabRow(selectedTabIndex = curTab.ordinal) {
                for (tab in ModpackTab.values()) {
                    LeadingIconTab(
                        selected = curTab == tab,
                        onClick = { curTab = tab },
                        text = { Text(tab.text) },
                        icon = tab.icon
                    )
                }
            }

            AnimatedContent(
                curTab, modifier = Modifier.weight(1f).fillMaxWidth(),
                transitionSpec = AnimatedContentScope<ModpackTab>::slidingTransitionSpec
            ) { tab ->
                when (tab) {
                    ModpackTab.DETAILS -> {
                        CurrentModpackDetailsView(component)
                    }
                    ModpackTab.MODS -> {
                        ModpackModsView(component)
                    }
                    ModpackTab.FILES -> {
                        ModpackFilesView(component)
                    }
                }
            }
        }
    }
}

enum class ModpackTab(val text: String, val icon: @Composable () -> Unit) {
    DETAILS("Details", { Icon(Icons.Default.Info, "details") }),
    MODS("Mods", { Icon(Icons.Default.List, "mods") }),
    FILES("Files", { Icon(MrMpBIcons.file, "files") });
}

@Composable
fun CurrentModpackDetailsView(component: ModpackComponent) {
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

@Composable
fun ModpackModsView(component: ModpackComponent) {
    Column {
        Box(
            modifier = Modifier.fillMaxWidth().shadow(5.dp, RectangleShape, false)
                .background(MaterialTheme.colors.surface)
        ) {
            Row(modifier = Modifier.padding(20.dp)) {
                Button(onClick = {}, modifier = Modifier.weight(1f)) {
                    Text("Add Mods...")
                }
            }
        }

        val lazyListState = rememberLazyListState()

        VerticalScrollWrapper(
            modifier = Modifier.weight(1f).fillMaxWidth(), adapter = ScrollbarAdapter(lazyListState),
            backgroundColor = Color.Transparent, backgroundShape = MaterialTheme.shapes.large, scrollbarPadding = 0.dp
        ) {
            LazyColumn(state = lazyListState, modifier = Modifier.padding(20.dp).weight(1f)) {
                items(component.modsList) { mod ->
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colors.surface, MaterialTheme.shapes.medium).padding(10.dp)
                            .fillMaxWidth()
                    ) {
                        Text(mod.toml.name)
                    }
                }
            }
        }
    }
}

@Composable
fun ModpackFilesView(component: ModpackComponent) {
    Box(Modifier.fillMaxSize()) {
        Text("Under construction...", modifier = Modifier.align(Alignment.Center))
    }
}
