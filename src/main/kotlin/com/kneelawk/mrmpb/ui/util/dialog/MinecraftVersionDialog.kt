package com.kneelawk.mrmpb.ui.util.dialog

import androidx.compose.foundation.ScrollbarAdapter
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import com.kneelawk.mrmpb.ui.util.widgets.ListButton
import com.kneelawk.mrmpb.ui.util.widgets.SmallButton
import com.kneelawk.mrmpb.ui.util.widgets.SmallSurface
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
    var selectedVersion by remember { mutableStateOf<MinecraftVersion?>(null) }

    val versionListState = rememberLazyListState()

    var showReleases by remember { mutableStateOf(true) }
    var showSnapshots by remember { mutableStateOf(false) }
    var showOldBetas by remember { mutableStateOf(false) }
    var showOldAlphas by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // race condition when setting selectedVersion :/
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

    LaunchedEffect(showReleases, showSnapshots, showOldBetas, showOldAlphas) {
        loading = true
        minecraftVersions.clear()
        minecraftVersions.addAll(MinecraftVersion.minecraftVersionList().filter {
            when (it.type) {
                MinecraftVersion.Type.OLD_ALPHA -> showOldAlphas
                MinecraftVersion.Type.OLD_BETA -> showOldBetas
                MinecraftVersion.Type.RELEASE -> showReleases
                MinecraftVersion.Type.SNAPSHOT -> showSnapshots
            }
        })

        // race condition when getting selectedVersion :/
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

    Column(modifier = Modifier.padding(20.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.h5, color = MrMpBTheme.colors.headingColor)

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
                                modifier = Modifier.fillMaxWidth().onPointerEvent(PointerEventType.Press) {
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
                SmallSurface(
                    onClick = { showReleases = !showReleases }, color = Color.Transparent,
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.defaultMinSize(minHeight = 36.dp).padding(horizontal = 20.dp)
                    ) {
                        Checkbox(
                            showReleases, null, enabled = !loading,
                            colors = CheckboxDefaults.colors(MaterialTheme.colors.primary)
                        )
                        Text("Releases", modifier = Modifier.padding(start = 5.dp))
                    }
                }

                SmallSurface(
                    onClick = { showSnapshots = !showSnapshots }, color = Color.Transparent,
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.defaultMinSize(minHeight = 36.dp).padding(horizontal = 20.dp)
                    ) {
                        Checkbox(
                            showSnapshots, null, enabled = !loading,
                            colors = CheckboxDefaults.colors(MaterialTheme.colors.primary)
                        )
                        Text("Snapshots", modifier = Modifier.padding(start = 5.dp))
                    }
                }

                SmallSurface(
                    onClick = { showOldBetas = !showOldBetas }, color = Color.Transparent,
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.defaultMinSize(minHeight = 36.dp).padding(horizontal = 20.dp)
                    ) {
                        Checkbox(
                            showOldBetas, null, enabled = !loading,
                            colors = CheckboxDefaults.colors(MaterialTheme.colors.primary)
                        )
                        Text("Old Betas", modifier = Modifier.padding(start = 5.dp))
                    }
                }

                SmallSurface(
                    onClick = { showOldAlphas = !showOldAlphas }, color = Color.Transparent,
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.defaultMinSize(minHeight = 36.dp).padding(horizontal = 20.dp)
                    ) {
                        Checkbox(
                            showOldAlphas, null, enabled = !loading,
                            colors = CheckboxDefaults.colors(MaterialTheme.colors.primary)
                        )
                        Text("Old Alphas", modifier = Modifier.padding(start = 5.dp))
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End), modifier = Modifier.fillMaxWidth()) {
            SmallButton(onClick = {
                finished(null)
            }, colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)) {
                Icon(Icons.Default.Close, "cancel")
                Text("Cancel", modifier = Modifier.padding(start = 5.dp))
            }

            SmallButton(onClick = {
                if (selectedVersion != null) {
                    finished(selectedVersion)
                }
            }, enabled = selectedVersion != null) {
                Icon(Icons.Default.Check, "select")
                Text("Select", modifier = Modifier.padding(start = 5.dp))
            }
        }
    }
}
