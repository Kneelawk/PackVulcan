package com.kneelawk.packvulcan.ui.modrinth.search

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.google.accompanist.flowlayout.FlowRow
import com.kneelawk.packvulcan.GlobalSettings
import com.kneelawk.packvulcan.model.LoaderVersion
import com.kneelawk.packvulcan.net.image.ImageResource
import com.kneelawk.packvulcan.ui.modrinth.DisplayElement
import com.kneelawk.packvulcan.ui.theme.PackVulcanIcons
import com.kneelawk.packvulcan.ui.theme.PackVulcanTheme
import com.kneelawk.packvulcan.ui.util.ModIconWrapper
import com.kneelawk.packvulcan.ui.util.layout.DialogContainerBox
import com.kneelawk.packvulcan.ui.util.layout.VerticalScrollWrapper
import com.kneelawk.packvulcan.ui.util.widgets.*
import com.kneelawk.packvulcan.util.LoadingState
import com.kneelawk.packvulcan.util.formatHumanReadable
import com.kneelawk.packvulcan.util.formatRelative
import kotlinx.coroutines.launch
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import java.time.Duration
import java.time.Instant

private val DROPDOWN_WAIT = Duration.ofMillis(200)

@Composable
fun ModrinthSearchWindow(
    onCloseRequest: () -> Unit, selectedMinecraftVersions: MutableMap<String, Unit>,
    selectedKnownLoaders: MutableMap<LoaderVersion.Type, Unit>, modpackName: String
) {
    val state = rememberWindowState(size = DpSize(1280.dp, 800.dp))

    Window(state = state, title = "Add from Modrinth to $modpackName", onCloseRequest = onCloseRequest) {
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp, end = 20.dp)
                ) {
                    SmallTextField(
                        value = controller.searchString,
                        onValueChange = { controller.setSearchString(it) },
                        modifier = Modifier.weight(1f)
                    )

                    Text("Sort by", modifier = Modifier.padding(start = 5.dp))

                    OptionsDropDown(
                        options = SearchIndexDisplay.values(),
                        buttonText = controller.sortBy.prettyName,
                        selectOption = controller::setSortBy
                    ) {
                        Text(it.prettyName)
                    }

                    Text("Show per page", modifier = Modifier.padding(start = 5.dp))

                    OptionsDropDown(
                        options = PerPageDisplay.values(),
                        buttonText = controller.perPage.limit.toString(),
                        selectOption = controller::setPerPage
                    ) {
                        Text(it.limit.toString())
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.fillMaxSize().padding(end = 20.dp)) {
                        Divider()

                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            LazyColumn(
                                state = controller.searchScrollState, modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(top = 10.dp, start = 0.dp, bottom = 10.dp, end = 0.dp)
                            ) {
                                items(controller.searchResults, { it.slug }) {
                                    SearchHitView(controller, it)
                                }
                            }

                            TopSideLoadingIndicator(controller.searchLoading)
                        }

                        Divider()
                    }

                    VerticalScrollbar(
                        adapter = rememberScrollbarAdapter(controller.searchScrollState),
                        modifier = Modifier.fillMaxHeight().align(Alignment.CenterEnd)
                    )
                }

                Row(
                    Modifier.padding(top = 10.dp, end = 20.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SmallButton(
                        onClick = { controller.pageBackward() },
                        minWidth = ButtonDefaults.MinHeight,
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colors.secondary),
                        enabled = controller.currentPage > 1
                    ) {
                        Text("<")
                    }

                    if (controller.finalPage <= 7) {
                        for (i in 1..controller.finalPage) {
                            val background =
                                if (i == controller.currentPage) MaterialTheme.colors.primary else MaterialTheme.colors.secondary

                            SmallButton(
                                onClick = { controller.goToPage(i) },
                                minWidth = ButtonDefaults.MinHeight,
                                colors = ButtonDefaults.buttonColors(backgroundColor = background)
                            ) {
                                Text("$i")
                            }
                        }
                    } else if (controller.currentPage < 5) {
                        for (i in 1..5) {
                            val background =
                                if (i == controller.currentPage) MaterialTheme.colors.primary else MaterialTheme.colors.secondary

                            SmallButton(
                                onClick = { controller.goToPage(i) },
                                minWidth = ButtonDefaults.MinHeight,
                                colors = ButtonDefaults.buttonColors(backgroundColor = background)
                            ) {
                                Text("$i")
                            }
                        }

                        Text("\u2014", modifier = Modifier.padding(horizontal = 10.dp))

                        SmallButton(
                            onClick = { controller.goToPage(controller.finalPage) },
                            minWidth = ButtonDefaults.MinHeight,
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colors.secondary)
                        ) {
                            Text("${controller.finalPage}")
                        }
                    } else if (controller.currentPage > controller.finalPage - 4) {
                        SmallButton(
                            onClick = { controller.goToPage(1) },
                            minWidth = ButtonDefaults.MinHeight,
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colors.secondary)
                        ) {
                            Text("1")
                        }

                        Text("\u2014", modifier = Modifier.padding(horizontal = 10.dp))

                        for (i in 1..5) {
                            val page = controller.finalPage - 5 + i
                            val background =
                                if (page == controller.currentPage) MaterialTheme.colors.primary else MaterialTheme.colors.secondary

                            SmallButton(
                                onClick = { controller.goToPage(page) },
                                minWidth = ButtonDefaults.MinHeight,
                                colors = ButtonDefaults.buttonColors(backgroundColor = background)
                            ) {
                                Text("$page")
                            }
                        }
                    } else {
                        SmallButton(
                            onClick = { controller.goToPage(1) },
                            minWidth = ButtonDefaults.MinHeight,
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colors.secondary)
                        ) {
                            Text("1")
                        }

                        Text("\u2014", modifier = Modifier.padding(horizontal = 10.dp))

                        SmallButton(
                            onClick = { controller.pageBackward() },
                            minWidth = ButtonDefaults.MinHeight,
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colors.secondary)
                        ) {
                            Text("${controller.currentPage - 1}")
                        }

                        SmallButton(
                            onClick = { },
                            minWidth = ButtonDefaults.MinHeight
                        ) {
                            Text("${controller.currentPage}")
                        }

                        SmallButton(
                            onClick = { controller.pageForward() },
                            minWidth = ButtonDefaults.MinHeight,
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colors.secondary)
                        ) {
                            Text("${controller.currentPage + 1}")
                        }

                        Text("\u2014", modifier = Modifier.padding(horizontal = 10.dp))

                        SmallButton(
                            onClick = { controller.goToPage(controller.finalPage) },
                            minWidth = ButtonDefaults.MinHeight,
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colors.secondary)
                        ) {
                            Text("${controller.finalPage}")
                        }
                    }

                    SmallButton(
                        onClick = { controller.pageForward() },
                        minWidth = ButtonDefaults.MinHeight,
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colors.secondary),
                        enabled = controller.currentPage < controller.finalPage
                    ) {
                        Text(">")
                    }
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
                        icon = {
                            item.icon?.icon(item.prettyName, Modifier.size(24.dp)) ?: Box(Modifier.size(24.dp))
                        },
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
private fun <T> OptionsDropDown(
    options: Array<T>, buttonText: String, selectOption: (T) -> Unit, menuItem: @Composable RowScope.(T) -> Unit
) {
    Box {
        var expanded by remember { mutableStateOf(false) }
        var expandTimer by remember { mutableStateOf(Instant.now()) }

        SmallButton(
            onClick = {
                if (expanded) {
                    expanded = false
                } else if (Duration.between(expandTimer, Instant.now()) >= DROPDOWN_WAIT) {
                    expanded = true
                }
            },
            colors = ButtonDefaults.buttonColors(MaterialTheme.colors.secondary)
        ) {
            Text(buttonText, modifier = Modifier.padding(end = 5.dp))
            Icon(Icons.Default.ArrowDropDown, "drop-down")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                expandTimer = Instant.now()
            }
        ) {
            for (option in options) {
                key(option) {
                    DropdownMenuItem(
                        onClick = {
                            expanded = false
                            selectOption(option)
                        }
                    ) {
                        menuItem(option)
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.TopSideLoadingIndicator(loading: Boolean) {
    AnimatedVisibility(
        visible = loading,
        modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        LinearProgressIndicator(Modifier.fillMaxWidth())
    }
}

@Composable
private fun SearchHitView(controller: ModrinthSearchInterface, searchHit: SearchHitDisplay) {
    val scope = rememberCoroutineScope()

    var modImage by remember { mutableStateOf<LoadingState<ModIconWrapper>>(LoadingState.Loading) }

    suspend fun loadModImage() {
        modImage = if (searchHit.iconUrl.isNullOrBlank()) {
            LoadingState.Loaded(ModIconWrapper.icon(PackVulcanIcons.noImage))
        } else {
            ImageResource.getModIcon(searchHit.iconUrl)?.let { LoadingState.Loaded(ModIconWrapper.image(it)) }
                ?: LoadingState.Error
        }
    }

    LaunchedEffect(Unit) {
        loadModImage()
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(15.dp), horizontalArrangement = Arrangement.spacedBy(15.dp),
            verticalAlignment = Alignment.Top
        ) {
            ModIcon(modImage) { scope.launch { loadModImage() } }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.width(IntrinsicSize.Max)
                ) {
                    Text(
                        searchHit.title, style = MaterialTheme.typography.h6,
                        color = PackVulcanTheme.colors.headingColor,
                        modifier = Modifier.weight(1f)
                    )

                    Text("by ${searchHit.author}")
                }

                Text(searchHit.description)

                FlowRow(mainAxisSpacing = 10.dp, crossAxisSpacing = 10.dp) {
                    for (category in searchHit.categories) {
                        key(category.apiName) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                category.icon?.icon(category.prettyName, Modifier.size(24.dp))
                                Text(category.prettyName)
                            }
                        }
                    }

                    for (loader in searchHit.loaders) {
                        key(loader.apiName) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                loader.icon?.icon(loader.prettyName, Modifier.size(24.dp))
                                Text(loader.prettyName)
                            }
                        }
                    }
                }

                FlowRow(mainAxisSpacing = 20.dp, crossAxisSpacing = 10.dp) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Create, "created")
                        Text("Created ${searchHit.dateCreated.formatRelative()}")
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Refresh, "updated")
                        Text("Updated ${searchHit.dateModified.formatRelative()}")
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.End) {
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(PackVulcanIcons.download, "downloads")
                    Text(searchHit.downloads.formatHumanReadable(), fontWeight = FontWeight.Bold)
                    Text("downloads")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(Icons.Default.Favorite, "follows")
                    Text(searchHit.follows.formatHumanReadable(), fontWeight = FontWeight.Bold)
                    Text("followers")
                }
            }
        }
    }
}
