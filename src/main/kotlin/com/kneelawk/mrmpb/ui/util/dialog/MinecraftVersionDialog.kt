package com.kneelawk.mrmpb.ui.util.dialog

import androidx.compose.foundation.ScrollbarAdapter
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.awtEventOrNull
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.rememberDialogState
import com.kneelawk.mrmpb.GlobalSettings
import com.kneelawk.mrmpb.model.MinecraftVersion
import com.kneelawk.mrmpb.ui.theme.MrMpBTheme
import com.kneelawk.mrmpb.ui.util.layout.ContainerBox
import com.kneelawk.mrmpb.ui.util.layout.VerticalScrollWrapper
import com.kneelawk.mrmpb.ui.util.widgets.DialogButtonBar
import com.kneelawk.mrmpb.ui.util.widgets.ListButton
import com.kneelawk.mrmpb.ui.util.widgets.SmallTextButton
import java.awt.event.MouseEvent

@Composable
fun MinecraftVersionDialog(title: String, previousVersion: String, finished: (MinecraftVersion?) -> Unit) {
    val state = rememberDialogState(width = 800.dp, height = 600.dp)

    Dialog(title = title, state = state, onCloseRequest = { finished(null) }) {
        MrMpBTheme(GlobalSettings.darkMode) {
            ContainerBox {
                MinecraftVersionView(title, previousVersion, finished)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MinecraftVersionView(title: String, previousVersion: String, finished: (MinecraftVersion?) -> Unit) {
    var loading by remember { mutableStateOf(true) }
    val minecraftVersions = remember { mutableListOf<MinecraftVersion>() }
    var selectedVersionSelected by remember { mutableStateOf(false) }
    var selectedVersion by remember { mutableStateOf<MinecraftVersion?>(null) }

    val versionListState = rememberLazyListState()

    var showReleases by remember { mutableStateOf(true) }
    var showSnapshots by remember { mutableStateOf(false) }
    var showOldBetas by remember { mutableStateOf(false) }
    var showOldAlphas by remember { mutableStateOf(false) }

    LaunchedEffect(showReleases, showSnapshots, showOldBetas, showOldAlphas) {
        loading = true

        if (!selectedVersionSelected) {
            selectedVersionSelected = true
            MinecraftVersion.forVersion(previousVersion).leftOrNull()?.let { version ->
                val valid = when (version.type) {
                    MinecraftVersion.Type.OLD_ALPHA -> showOldAlphas
                    MinecraftVersion.Type.OLD_BETA -> showOldBetas
                    MinecraftVersion.Type.RELEASE -> showReleases
                    MinecraftVersion.Type.SNAPSHOT -> showSnapshots
                }
                if (valid) {
                    selectedVersion = version
                }
            }
        }

        minecraftVersions.clear()
        minecraftVersions.addAll(MinecraftVersion.minecraftVersionList().filter {
            when (it.type) {
                MinecraftVersion.Type.OLD_ALPHA -> showOldAlphas
                MinecraftVersion.Type.OLD_BETA -> showOldBetas
                MinecraftVersion.Type.RELEASE -> showReleases
                MinecraftVersion.Type.SNAPSHOT -> showSnapshots
            }
        })

        val selected = selectedVersion
        if (selected != null) {
            val index = minecraftVersions.indexOf(selected)
            if (index < 0) {
                selectedVersion = null
            } else {
                versionListState.scrollToItem(index, -50)
            }
        }

        loading = false
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text(title) })
    }) {
        Column(modifier = Modifier.padding(20.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    VerticalScrollWrapper(
                        modifier = Modifier.fillMaxSize(), adapter = ScrollbarAdapter(versionListState)
                    ) {
                        LazyColumn(modifier = Modifier.weight(1f).fillMaxHeight(), state = versionListState) {
                            items(minecraftVersions) { version ->
                                val background = if (selectedVersion == version) {
                                    MaterialTheme.colors.secondary
                                } else {
                                    Color.Transparent
                                }

                                ListButton(
                                    onClick = {
                                        selectedVersion = version
                                    },
                                    modifier = Modifier.fillMaxWidth().onPointerEvent(PointerEventType.Release) {
                                        if (it.awtEventOrNull?.button == MouseEvent.BUTTON1 && it.awtEventOrNull?.clickCount == 2) {
                                            // a double click counts as a selection
                                            finished(version)
                                        }
                                    }, colors = ButtonDefaults.textButtonColors(backgroundColor = background),
                                    enabled = !loading
                                ) {
                                    Text(version.toString())
                                }
                            }
                        }
                    }

                    if (loading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SmallTextButton(onClick = { showReleases = !showReleases }, enabled = !loading) {
                        Checkbox(
                            showReleases, null, enabled = !loading,
                            colors = CheckboxDefaults.colors(MaterialTheme.colors.primary)
                        )
                        Text("Releases", modifier = Modifier.padding(start = 5.dp))
                    }

                    SmallTextButton(onClick = { showSnapshots = !showSnapshots }, enabled = !loading) {
                        Checkbox(
                            showSnapshots, null, enabled = !loading,
                            colors = CheckboxDefaults.colors(MaterialTheme.colors.primary)
                        )
                        Text("Snapshots", modifier = Modifier.padding(start = 5.dp))
                    }

                    SmallTextButton(onClick = { showOldBetas = !showOldBetas }, enabled = !loading) {
                        Checkbox(
                            showOldBetas, null, enabled = !loading,
                            colors = CheckboxDefaults.colors(MaterialTheme.colors.primary)
                        )
                        Text("Old Betas", modifier = Modifier.padding(start = 5.dp))
                    }

                    SmallTextButton(onClick = { showOldAlphas = !showOldAlphas }, enabled = !loading) {
                        Checkbox(
                            showOldAlphas, null, enabled = !loading,
                            colors = CheckboxDefaults.colors(MaterialTheme.colors.primary)
                        )
                        Text("Old Alphas", modifier = Modifier.padding(start = 5.dp))
                    }
                }
            }

            DialogButtonBar(
                modifier = Modifier.fillMaxWidth(),
                onCancel = { finished(null) },
                onConfirm = {
                    if (selectedVersion != null) {
                        finished(selectedVersion)
                    }
                },
                confirmEnabled = selectedVersion != null
            )
        }
    }
}
