package com.kneelawk.mrmpb.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.dp
import com.kneelawk.mrmpb.ui.keyboard.KeyboardTracker
import com.kneelawk.mrmpb.ui.keyboard.shortcut
import com.kneelawk.mrmpb.ui.keyboard.shortcuts
import com.kneelawk.mrmpb.ui.theme.MrMpBIcons
import com.kneelawk.mrmpb.ui.util.layout.AppContainerBox
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
    FILES("Files", { Icon(MrMpBIcons.file, "files") });
}
