package com.kneelawk.mrmpb.ui.util.dialog

import androidx.compose.animation.*
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
import com.kneelawk.mrmpb.model.LoaderVersion
import com.kneelawk.mrmpb.ui.theme.MrMpBIcons
import com.kneelawk.mrmpb.ui.theme.MrMpBTheme
import com.kneelawk.mrmpb.ui.util.layout.DialogContainerBox
import com.kneelawk.mrmpb.ui.util.layout.VerticalScrollWrapper
import com.kneelawk.mrmpb.ui.util.layout.slidingTransitionSpec
import com.kneelawk.mrmpb.ui.util.widgets.DialogButtonBar
import com.kneelawk.mrmpb.ui.util.widgets.ListButton
import com.kneelawk.mrmpb.ui.util.widgets.SmallTextButton
import com.kneelawk.mrmpb.ui.util.widgets.SmallTextField
import java.awt.event.MouseEvent

@Composable
fun LoaderVersionDialog(
    title: String, previousVersion: String, minecraftVersion: String, finished: (LoaderVersion?) -> Unit
) {
    val state = rememberDialogState(width = 800.dp, height = 600.dp)

    Dialog(title = title, state = state, onCloseRequest = { finished(null) }) {
        MrMpBTheme(GlobalSettings.darkMode) {
            DialogContainerBox {
                LoaderVersionView(title, previousVersion, minecraftVersion, finished)
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoaderVersionView(
    title: String, previousVersion: String, minecraftVersion: String, finished: (LoaderVersion?) -> Unit
) {
    var selectedVersion by remember { mutableStateOf<LoaderVersion?>(null) }
    var selectedVersionSelected by remember { mutableStateOf(false) }
    var curTab by remember {
        mutableStateOf(
            LoaderType.fromLoaderVersionType(
                LoaderVersion.Type.forVersion(previousVersion)
                    ?: LoaderVersion.Type.QUILT
            )
        )
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text(title) })
    }) {
        Column {
            TabRow(selectedTabIndex = curTab.ordinal) {
                for (ty in LoaderType.values()) {
                    LeadingIconTab(
                        selected = curTab == ty,
                        onClick = { curTab = ty },
                        text = { Text(ty.text) },
                        icon = ty.icon
                    )
                }
            }

            AnimatedContent(
                curTab, modifier = Modifier.weight(1f).fillMaxWidth(),
                transitionSpec = AnimatedContentScope<LoaderType>::slidingTransitionSpec
            ) { tab ->
                when (tab) {
                    LoaderType.FABRIC -> {
                        IndependentLoaderVersionSelector(
                            selectedVersion = selectedVersion,
                            selectedVersionChange = { selectedVersion = it },
                            selectedVersionSelected = selectedVersionSelected,
                            applySelectedVersionSelected = { selectedVersionSelected = true },
                            previousVersion = previousVersion,
                            finished = finished
                        ) {
                            LoaderVersion.fabricLoaderList()
                        }
                    }
                    LoaderType.QUILT -> {
                        IndependentLoaderVersionSelector(
                            selectedVersion = selectedVersion,
                            selectedVersionChange = { selectedVersion = it },
                            selectedVersionSelected = selectedVersionSelected,
                            applySelectedVersionSelected = { selectedVersionSelected = true },
                            previousVersion = previousVersion,
                            finished = finished
                        ) {
                            LoaderVersion.quiltLoaderList()
                        }
                    }
                    LoaderType.FORGE -> {
                        DependentLoaderVersionSelector(
                            selectedVersion = selectedVersion,
                            selectedVersionChange = { selectedVersion = it },
                            selectedVersionSelected = selectedVersionSelected,
                            applySelectedVersionSelected = { selectedVersionSelected = true },
                            previousVersion = previousVersion,
                            finished = finished
                        ) {
                            LoaderVersion.forgeList(
                                if (it) {
                                    minecraftVersion
                                } else {
                                    null
                                }
                            )
                        }
                    }
                }
            }

            SmallTextField(
                value = selectedVersion?.toString() ?: "", onValueChange = {},
                modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()
            )

            DialogButtonBar(
                modifier = Modifier.padding(top = 10.dp, bottom = 20.dp, start = 20.dp, end = 20.dp).fillMaxWidth(),
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun IndependentLoaderVersionSelector(
    selectedVersion: LoaderVersion?, selectedVersionChange: (LoaderVersion?) -> Unit, selectedVersionSelected: Boolean,
    applySelectedVersionSelected: () -> Unit, previousVersion: String, finished: (LoaderVersion?) -> Unit,
    loadVersions: suspend () -> List<LoaderVersion>,
) {
    val loaderVersions = remember { mutableListOf<LoaderVersion>() }
    var loading by remember { mutableStateOf(true) }

    val versionListState = rememberLazyListState()

    LaunchedEffect(Unit) {
        loading = true

        val selected = if (!selectedVersionSelected) {
            applySelectedVersionSelected()
            val version = LoaderVersion.forVersion(previousVersion, null).switch({ it }, { null })
            selectedVersionChange(version)
            version
        } else {
            selectedVersion
        }

        loaderVersions.clear()
        loaderVersions.addAll(loadVersions())

        if (selected != null) {
            val index = loaderVersions.indexOf(selected)
            if (index >= 0) {
                versionListState.scrollToItem(index, -50)
            }
        }

        loading = false
    }

    Box(modifier = Modifier.padding(20.dp).fillMaxSize()) {
        VerticalScrollWrapper(
            modifier = Modifier.fillMaxSize(), adapter = ScrollbarAdapter(versionListState)
        ) {
            LazyColumn(modifier = Modifier.weight(1f).fillMaxHeight(), state = versionListState) {
                items(loaderVersions) { version ->
                    val background = if (selectedVersion?.toString() == version.toString()) {
                        MaterialTheme.colors.secondary
                    } else {
                        Color.Transparent
                    }

                    ListButton(
                        onClick = {
                            selectedVersionChange(version)
                        },
                        modifier = Modifier.fillMaxWidth().onPointerEvent(PointerEventType.Release) {
                            if (it.awtEventOrNull?.button == MouseEvent.BUTTON1 && it.awtEventOrNull?.clickCount == 2) {
                                // a double click counts as a selection
                                finished(version)
                            }
                        }, colors = ButtonDefaults.textButtonColors(backgroundColor = background),
                        enabled = !loading,
                        text = version.toString()
                    )
                }
            }
        }

        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun DependentLoaderVersionSelector(
    selectedVersion: LoaderVersion?, selectedVersionChange: (LoaderVersion?) -> Unit, selectedVersionSelected: Boolean,
    applySelectedVersionSelected: () -> Unit, previousVersion: String, finished: (LoaderVersion?) -> Unit,
    loadVersions: suspend (Boolean) -> List<LoaderVersion>,
) {
    val loaderVersions = remember { mutableListOf<LoaderVersion>() }
    var loading by remember { mutableStateOf(true) }
    var filterByMcVersion by remember { mutableStateOf(true) }

    val versionListState = rememberLazyListState()

    LaunchedEffect(filterByMcVersion) {
        loading = true

        val selected = if (!selectedVersionSelected) {
            applySelectedVersionSelected()
            val version = LoaderVersion.forVersion(previousVersion, null).switch({ it }, { null })
            selectedVersionChange(version)
            version
        } else {
            selectedVersion
        }

        loaderVersions.clear()
        loaderVersions.addAll(loadVersions(filterByMcVersion))

        if (selected != null) {
            val index = loaderVersions.indexOf(selected)
            if (index >= 0) {
                versionListState.scrollToItem(index, -50)
            }
        }

        loading = false
    }

    Column(modifier = Modifier.padding(20.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SmallTextButton(onClick = { filterByMcVersion = !filterByMcVersion }, enabled = !loading) {
            Checkbox(
                filterByMcVersion, null, enabled = !loading,
                colors = CheckboxDefaults.colors(MaterialTheme.colors.primary)
            )
            Text("Filter by Minecraft version", modifier = Modifier.padding(start = 5.dp))
        }

        Box(modifier = Modifier.weight(1f)) {
            VerticalScrollWrapper(
                modifier = Modifier.fillMaxSize(), adapter = ScrollbarAdapter(versionListState)
            ) {
                LazyColumn(modifier = Modifier.weight(1f).fillMaxHeight(), state = versionListState) {
                    items(loaderVersions) { version ->
                        val background = if (selectedVersion?.toString() == version.toString()) {
                            MaterialTheme.colors.secondary
                        } else {
                            Color.Transparent
                        }

                        ListButton(
                            onClick = {
                                selectedVersionChange(version)
                            },
                            modifier = Modifier.fillMaxWidth().onPointerEvent(PointerEventType.Release) {
                                if (it.awtEventOrNull?.button == MouseEvent.BUTTON1 && it.awtEventOrNull?.clickCount == 2) {
                                    // a double click counts as a selection
                                    finished(version)
                                }
                            }, colors = ButtonDefaults.textButtonColors(backgroundColor = background),
                            enabled = !loading,
                            text = version.toString()
                        )
                    }
                }
            }

            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

private enum class LoaderType(val text: String, val icon: @Composable () -> Unit) {
    FABRIC("Fabric", { Icon(MrMpBIcons.fabric, "fabric") }),
    QUILT("Quilt", { Icon(MrMpBIcons.quilt, "quilt") }),
    FORGE("Forge", { Icon(MrMpBIcons.forge, "forge") });

    companion object {
        fun fromLoaderVersionType(type: LoaderVersion.Type): LoaderType {
            return when (type) {
                LoaderVersion.Type.FABRIC -> FABRIC
                LoaderVersion.Type.FORGE -> FORGE
                LoaderVersion.Type.QUILT -> QUILT
            }
        }
    }
}
