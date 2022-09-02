package com.kneelawk.packvulcan.ui.modrinth.search

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kneelawk.packvulcan.ui.modrinth.PRIMARY_MOD_LOADERS
import com.kneelawk.packvulcan.ui.theme.PackVulcanIcons
import com.kneelawk.packvulcan.ui.util.layout.VerticalScrollWrapper
import com.kneelawk.packvulcan.ui.util.widgets.CheckboxButton
import com.kneelawk.packvulcan.ui.util.widgets.SmallButton
import com.kneelawk.packvulcan.ui.util.widgets.SmallIconButton

@Composable
fun FilterSidebar(controller: ModrinthSearchInterface) {
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

                        Box(contentAlignment = Alignment.Center) {
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
                                                        controller.unselectMinecraftVersion(version.version)
                                                    } else {
                                                        controller.selectMinecraftVersion(version.version)
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

                        CollapsableLoadableList(
                            list = controller.loaderList,
                            selected = controller.selectedLoaders,
                            selectItem = controller::selectLoader,
                            unselectItem = controller::unselectLoader,
                            itemAlwaysVisible = { PRIMARY_MOD_LOADERS.contains(it.apiName) },
                            enabled = controller.loaderSelectorEnabled,
                            loading = controller.loaderSelectorLoading
                        )
                    }

                    Divider()

                    Column {
                        Text("Environments", fontWeight = FontWeight.Bold)

                        Row {
                            CheckboxButton(
                                checked = controller.filterClient,
                                onClick = { controller.setFilterClient(!controller.filterClient) },
                                modifier = Modifier.weight(1f),
                                icon = { Icon(PackVulcanIcons.laptop, "client") },
                                text = "Client"
                            )
                            CheckboxButton(
                                checked = controller.filterServer,
                                onClick = { controller.setFilterServer(!controller.filterServer) },
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
                            list = controller.categoryList,
                            selected = controller.selectedCategories,
                            selectItem = controller::selectCategory,
                            unselectItem = controller::unselectCategory,
                            enabled = controller.categorySelectorEnabled,
                            loading = controller.categorySelectorLoading
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

        SmallButton(onClick = { controller.clearFilters() }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Clear, "clear")

            Text("Clear Filters", modifier = Modifier.padding(start = 10.dp))
        }
    }
}
