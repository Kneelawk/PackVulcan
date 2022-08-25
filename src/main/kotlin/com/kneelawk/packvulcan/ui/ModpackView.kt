package com.kneelawk.packvulcan.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.dp
import com.kneelawk.packvulcan.engine.packwiz.PackwizProject
import com.kneelawk.packvulcan.ui.attributor.AttributorView
import com.kneelawk.packvulcan.ui.keyboard.KeyboardTracker
import com.kneelawk.packvulcan.ui.keyboard.shortcut
import com.kneelawk.packvulcan.ui.keyboard.shortcuts
import com.kneelawk.packvulcan.ui.theme.PackVulcanIcons
import com.kneelawk.packvulcan.ui.util.dialog.file.OpenFileDialog
import com.kneelawk.packvulcan.ui.util.layout.AppContainerBox
import com.kneelawk.packvulcan.ui.util.layout.slidingTransitionSpec
import com.kneelawk.packvulcan.ui.util.widgets.ListButton
import kotlin.io.path.isDirectory

const val LOADING_MODPACK_TEXT = "Loading..."

@OptIn(ExperimentalComposeUiApi::class)
private val NEW_SHORTCUT = shortcut().cmd().key(Key.N).finish()

@OptIn(ExperimentalComposeUiApi::class)
private val OPEN_SHORTCUT = shortcut().cmd().key(Key.O).finish()

@OptIn(ExperimentalComposeUiApi::class)
private val SAVE_SHORTCUT = shortcut().cmd().key(Key.S).finish()

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ModpackView(component: ModpackComponent, controls: WindowControls, tracker: KeyboardTracker) {
    var curTab by remember { mutableStateOf(ModpackTab.DETAILS) }

    shortcuts(
        tracker,
        NEW_SHORTCUT to {
            component.new()
        },
        OPEN_SHORTCUT to {
            component.open()
        },
        SAVE_SHORTCUT to {
            component.save()
        }
    )

    LaunchedEffect(component.modpackName) {
        controls.title = component.modpackName.ifBlank { LOADING_MODPACK_TEXT }
    }

    ModpackDialogs(component)
    ModpackModsDialogs(component)

    AppContainerBox(
        title = component.modpackName,
        extraDrawerContent = {
            ListButton(
                onClick = { component.new() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Create, "new")
                Text("New", modifier = Modifier.padding(start = 10.dp))
                Box(Modifier.weight(1f))
                Text(NEW_SHORTCUT.toString())
            }

            ListButton(
                onClick = { component.open() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(PackVulcanIcons.file, "open")
                Text("Open", modifier = Modifier.padding(start = 10.dp))
                Box(Modifier.weight(1f))
                Text(OPEN_SHORTCUT.toString())
            }

            Divider()

            ListButton(
                onClick = { component.save() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(PackVulcanIcons.save, "save")
                Text("Save", modifier = Modifier.padding(start = 10.dp))
                Box(Modifier.weight(1f))
                Text(SAVE_SHORTCUT.toString())
            }

            Divider()

            ListButton(
                onClick = { component.attributorDialogOpen = true },
                enabled = !component.attributorDialogOpen,
                modifier = Modifier.fillMaxWidth(),
                text = "Attributor"
            )
        },
        actions = {
            IconButton(onClick = {
                component.reload()
            }) {
                Icon(Icons.Default.Refresh, "refresh")
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
                        ModpackCurrentDetailsView(component)
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
    FILES("Files", { Icon(PackVulcanIcons.file, "files") });
}

@Composable
private fun ModpackDialogs(component: ModpackComponent) {
    if (component.openDialogOpen) {
        OpenFileDialog(
            title = "Open Packwiz 'pack.toml'",
            visibilityFilter = { it.isDirectory() || PackwizProject.isPackFile(it) }
        ) { selected ->
            component.openDialogOpen = false
            selected?.let { component.open(it) }
        }
    }

    if (component.attributorDialogOpen) {
        AttributorView(
            onCloseRequest = { component.attributorDialogOpen = false },
            modsList = component.modsList
        )
    }
}
