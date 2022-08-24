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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import com.kneelawk.packvulcan.model.modrinth.search.result.SearchHitJson
import com.kneelawk.packvulcan.net.image.ImageResource
import com.kneelawk.packvulcan.ui.theme.PackVulcanIcons
import com.kneelawk.packvulcan.ui.theme.PackVulcanTheme
import com.kneelawk.packvulcan.ui.util.ImageWrapper
import com.kneelawk.packvulcan.ui.util.layout.DialogContainerBox
import com.kneelawk.packvulcan.ui.util.layout.VerticalScrollWrapper
import com.kneelawk.packvulcan.ui.util.widgets.*
import com.kneelawk.packvulcan.util.LoadingState
import kotlinx.coroutines.launch
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
                                    controller.categoryList,
                                    controller.selectedCategories,
                                    controller::selectCategory,
                                    controller::unselectCategory,
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
            Column(
                Modifier.padding(top = 20.dp, start = (5 - 1.5).dp, bottom = 20.dp, end = 0.dp)
            ) {
                Column(Modifier.padding(end = 20.dp)) {
                    SmallTextField(
                        value = controller.searchString,
                        onValueChange = { controller.setSearchString(it) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                    )

                    AnimatedVisibility(controller.searchLoading) {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    }
                }

                val searchScrollState = rememberLazyListState()

                Row(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Divider()

                        LazyColumn(
                            state = searchScrollState, modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(top = 10.dp, start = 0.dp, bottom = 10.dp, end = 0.dp)
                        ) {
                            items(controller.searchResults, { it.slug }) {
                                SearchHitView(controller, it)
                            }
                        }

                        Divider()
                    }

                    VerticalScrollbar(
                        adapter = rememberScrollbarAdapter(searchScrollState),
                        modifier = Modifier.fillMaxHeight()
                    )
                }
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

@Composable
private fun SearchHitView(controller: ModrinthSearchInterface, searchHit: SearchHitJson) {
    val scope = rememberCoroutineScope()

    var modImage by remember { mutableStateOf<LoadingState<ImageWrapper>>(LoadingState.Loading) }

    suspend fun loadModImage() {
        modImage = if (searchHit.iconUrl.isNullOrBlank()) {
            LoadingState.Loaded(ImageWrapper.Painter(PackVulcanIcons.noImage))
        } else {
            ImageResource.getModIcon(searchHit.iconUrl)?.let { LoadingState.Loaded(ImageWrapper.ImageBitmap(it)) }
                ?: LoadingState.Error
        }
    }

    LaunchedEffect(Unit) {
        loadModImage()
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            ModIcon(modImage) { scope.launch { loadModImage() } }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        searchHit.title, style = MaterialTheme.typography.h6,
                        color = PackVulcanTheme.colors.headingColor
                    )

                    Text("by ${searchHit.author}")
                }

                Text(searchHit.description)
            }
        }
    }
}
