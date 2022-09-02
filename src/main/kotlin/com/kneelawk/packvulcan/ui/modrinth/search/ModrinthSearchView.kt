package com.kneelawk.packvulcan.ui.modrinth.search

import androidx.compose.animation.*
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.kneelawk.packvulcan.GlobalSettings
import com.kneelawk.packvulcan.model.AcceptableVersions
import com.kneelawk.packvulcan.model.LoaderVersion
import com.kneelawk.packvulcan.ui.InstallOperation
import com.kneelawk.packvulcan.ui.theme.PackVulcanTheme
import com.kneelawk.packvulcan.ui.util.layout.DialogContainerBox
import com.kneelawk.packvulcan.ui.util.widgets.SmallTextField
import com.kneelawk.packvulcan.ui.util.widgets.styledSplitter
import com.kneelawk.packvulcan.util.MSet
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState

@Composable
fun ModrinthSearchWindow(
    onCloseRequest: () -> Unit, selectedMinecraftVersions: MutableMap<String, Unit>,
    selectedKnownLoaders: MutableMap<LoaderVersion.Type, Unit>, modpackName: String,
    acceptableVersions: AcceptableVersions, modrinthProjects: MSet<String>, openProject: (id: String) -> Unit,
    install: (InstallOperation) -> Unit, browseVersions: (id: String) -> Unit
) {
    val state = rememberWindowState(size = DpSize(1280.dp, 800.dp))

    Window(state = state, title = "Add from Modrinth to $modpackName", onCloseRequest = onCloseRequest) {
        PackVulcanTheme(GlobalSettings.darkMode) {
            DialogContainerBox {
                ModrinthSearchView(
                    rememberModrinthSearchController(
                        selectedMinecraftVersions = selectedMinecraftVersions,
                        selectedKnownLoaders = selectedKnownLoaders,
                        acceptableVersions = acceptableVersions,
                        modrinthProjects = modrinthProjects,
                        openProject = openProject,
                        install = install,
                        browseVersions = browseVersions
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
    val focusRequester = remember { FocusRequester() }

    HorizontalSplitPane(splitPaneState = splitPaneState, modifier = Modifier.fillMaxHeight()) {
        first(200.dp) {
            FilterSidebar(controller)
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
                        modifier = Modifier.weight(1f).focusRequester(focusRequester),
                        permanentIcon = {
                            Icon(Icons.Default.Search, "search")
                        },
                        ghostText = {
                            Text("Search mods...")
                        }
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

                PaginationBar(controller)
            }
        }

        styledSplitter()
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
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
