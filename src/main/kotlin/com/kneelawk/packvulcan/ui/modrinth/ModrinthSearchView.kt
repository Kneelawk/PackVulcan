package com.kneelawk.packvulcan.ui.modrinth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.kneelawk.packvulcan.GlobalSettings
import com.kneelawk.packvulcan.model.LoaderVersion
import com.kneelawk.packvulcan.ui.theme.PackVulcanIcons
import com.kneelawk.packvulcan.ui.theme.PackVulcanTheme
import com.kneelawk.packvulcan.ui.util.layout.DialogContainerBox
import com.kneelawk.packvulcan.ui.util.layout.VerticalScrollWrapper
import com.kneelawk.packvulcan.ui.util.widgets.CheckboxButton
import com.kneelawk.packvulcan.ui.util.widgets.SmallIconButton
import com.kneelawk.packvulcan.ui.util.widgets.styledSplitter
import com.kneelawk.packvulcan.util.add
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState

@Composable
fun ModrinthSearchWindow(
    onCloseRequest: () -> Unit, selectedMinecraftVersions: MutableMap<String, Unit>,
    selectedKnownLoaders: MutableMap<LoaderVersion.Type, Unit>
) {
    val state = rememberWindowState(size = DpSize(1280.dp, 800.dp))

    Window(state = state, title = "Add from Modrinth", onCloseRequest = onCloseRequest) {
        PackVulcanTheme(GlobalSettings.darkMode) {
            DialogContainerBox {
                ModrinthSearchView(
                    rememberModrinthSearchController(
                        selectedMinecraftVersions = selectedMinecraftVersions,
                        selectedKnownLoaders = selectedKnownLoaders,
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalSplitPaneApi::class)
@Composable
fun ModrinthSearchView(controller: ModrinthSearchInterface) {
    val splitPaneState = rememberSplitPaneState(0.55f)

    HorizontalSplitPane(splitPaneState = splitPaneState, modifier = Modifier.fillMaxHeight()) {
        first(200.dp) {
            Column(
                Modifier.padding(top = 20.dp, start = 20.dp, bottom = 20.dp, end = (5 - 1.5).dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(MaterialTheme.colors.surface, MaterialTheme.shapes.medium)
                        .clip(MaterialTheme.shapes.medium)
                ) {
                    val sidebarScrollState = rememberScrollState()

                    Row(modifier = Modifier.fillMaxSize()) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(15.dp),
                            modifier = Modifier.fillMaxHeight().weight(1f).verticalScroll(sidebarScrollState)
                                .padding(15.dp)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.heightIn(30.dp, 400.dp)
                            ) {
                                Text("Minecraft versions", fontWeight = FontWeight.Bold)

                                Row {
                                    SmallIconButton(
                                        onClick = {
                                            controller.showMinecraftExtraTypes = !controller.showMinecraftExtraTypes
                                        }
                                    ) {
                                        val iconRotation by animateFloatAsState(
                                            if (controller.showMinecraftExtraTypes) 0f else -90f
                                        )
                                        Icon(
                                            Icons.Default.ArrowDropDown, "drop-down",
                                            modifier = Modifier.rotate(iconRotation)
                                        )
                                    }

                                    Column {
                                        AnimatedVisibility(controller.showMinecraftExtraTypes) {
                                            CheckboxButton(
                                                checked = controller.showMinecraftReleases,
                                                onClick = {
                                                    controller.showMinecraftReleases = !controller.showMinecraftReleases
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                enabled = controller.minecraftSelectorEnabled,
                                                text = "Show Releases"
                                            )
                                        }

                                        CheckboxButton(
                                            checked = controller.showMinecraftSnapshots,
                                            onClick = {
                                                controller.showMinecraftSnapshots = !controller.showMinecraftSnapshots
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            enabled = controller.minecraftSelectorEnabled,
                                            text = "Show Snapshots"
                                        )

                                        AnimatedVisibility(controller.showMinecraftExtraTypes) {
                                            Column {
                                                CheckboxButton(
                                                    checked = controller.showMinecraftBetas,
                                                    onClick = {
                                                        controller.showMinecraftBetas = !controller.showMinecraftBetas
                                                    },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    enabled = controller.minecraftSelectorEnabled,
                                                    text = "Show Betas"
                                                )
                                                CheckboxButton(
                                                    checked = controller.showMinecraftAlphas,
                                                    onClick = {
                                                        controller.showMinecraftAlphas = !controller.showMinecraftAlphas
                                                    },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    enabled = controller.minecraftSelectorEnabled,
                                                    text = "Show Alphas"
                                                )
                                            }
                                        }
                                    }
                                }

                                Box {
                                    val minecraftVersionScrollState = rememberLazyListState()

                                    VerticalScrollWrapper(
                                        adapter = rememberScrollbarAdapter(minecraftVersionScrollState),
                                        backgroundShape = MaterialTheme.shapes.large,
                                        backgroundColor = Color.Transparent,
                                        scrollbarPadding = PaddingValues(0.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Divider()

                                            LazyColumn(
                                                state = minecraftVersionScrollState, modifier = Modifier.weight(1f)
                                            ) {
                                                items(controller.minecraftVersionList) { version ->
                                                    CheckboxButton(
                                                        checked = controller.selectedMinecraftVersions.contains(
                                                            version.version
                                                        ),
                                                        onClick = {
                                                            val selected = controller.selectedMinecraftVersions
                                                            if (selected.contains(version.version)) {
                                                                selected.remove(version.version)
                                                            } else {
                                                                selected.add(version.version)
                                                            }
                                                        },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        enabled = controller.minecraftSelectorEnabled,
                                                        text = version.toString()
                                                    )
                                                }
                                            }

                                            Divider()
                                        }
                                    }

                                    if (controller.minecraftSelectorLoading) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }

                            Divider()

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Mod Loaders", fontWeight = FontWeight.Bold)

                                StaticLoadableList(
                                    controller.loaderList,
                                    controller.selectedLoaders,
                                    controller::selectLoader,
                                    controller::unselectLoader,
                                    controller.loaderSelectorEnabled,
                                    controller.loaderSelectorLoading
                                )
                            }

                            Divider()

                            Column {
                                Text("Environments", fontWeight = FontWeight.Bold)

                                Row {
                                    CheckboxButton(
                                        checked = controller.filterClient,
                                        onClick = { controller.filterClient = !controller.filterClient },
                                        modifier = Modifier.weight(1f),
                                        icon = { Icon(PackVulcanIcons.laptop, "client") },
                                        text = "Client"
                                    )
                                    CheckboxButton(
                                        checked = controller.filterServer,
                                        onClick = { controller.filterServer = !controller.filterServer },
                                        modifier = Modifier.weight(1f),
                                        icon = { Icon(PackVulcanIcons.storage, "server") },
                                        text = "Server"
                                    )
                                }
                            }

                            Divider()

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Categories", fontWeight = FontWeight.Bold)

                                StaticLoadableList(
                                    controller.categoryList,
                                    controller.selectedCategories,
                                    controller.selectedCategories::add,
                                    controller.selectedCategories::remove,
                                    controller.categorySelectorEnabled,
                                    controller.categorySelectorLoading
                                )
                            }

                            Divider()

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Licenses", fontWeight = FontWeight.Bold)

                                Text("Todo")
                            }
                        }

                        VerticalScrollbar(
                            adapter = rememberScrollbarAdapter(sidebarScrollState),
                            modifier = Modifier.padding(vertical = 15.dp)
                        )
                    }
                }

                Button(onClick = { controller.clearFilters() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Clear, "clear")

                    Text("Clear Filters", modifier = Modifier.padding(start = 10.dp))
                }
            }
        }

        second(400.dp) {
            Column {

            }
        }

        styledSplitter()
    }
}

@Composable
private fun <T : DisplayElement> StaticLoadableList(
    list: List<T>,
    selected: Map<T, Unit>,
    selectItem: (T) -> Unit,
    unselectItem: (T) -> Unit,
    enabled: Boolean, loading: Boolean
) {
    Box {
        Column {
            for (item in list) {
                key(item) {
                    CheckboxButton(
                        checked = selected.contains(item),
                        onClick = {
                            if (selected.contains(item)) {
                                unselectItem(item)
                            } else {
                                selectItem(item)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = enabled,
                        icon = item.icon,
                        text = item.prettyName
                    )
                }
            }
        }

        if (loading) {
            CircularProgressIndicator()
        }
    }
}
