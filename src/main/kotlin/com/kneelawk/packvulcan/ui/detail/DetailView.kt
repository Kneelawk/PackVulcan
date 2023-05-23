package com.kneelawk.packvulcan.ui.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.kneelawk.packvulcan.GlobalSettings
import com.kneelawk.packvulcan.ui.theme.PackVulcanTheme
import com.kneelawk.packvulcan.ui.util.layout.DialogContainerBox
import com.kneelawk.packvulcan.ui.util.layout.slidingTransitionSpec
import com.kneelawk.packvulcan.ui.util.widgets.ReloadableIcon
import com.kneelawk.packvulcan.ui.util.widgets.SmallTextButton
import com.kneelawk.packvulcan.ui.util.widgets.TopSideLoadingIndicator
import com.kneelawk.packvulcan.ui.util.widgets.styledSplitter
import com.kneelawk.packvulcan.util.LoadingState
import kotlinx.coroutines.launch
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState

@Composable
fun DetailWindow(onCloseRequest: () -> Unit, selector: DetailSelector) {
    val state = rememberWindowState(size = DpSize(1280.dp, 800.dp))

    var title by remember { mutableStateOf("Loading...") }

    Window(onCloseRequest = onCloseRequest, state = state, title = title) {
        PackVulcanTheme(GlobalSettings.darkMode) {
            DialogContainerBox {
                DetailView(rememberDetailController(selector = selector, updateTitle = { title = it }))
            }
        }
    }
}

@OptIn(ExperimentalSplitPaneApi::class)
@Composable
fun DetailView(controller: DetailInterface) {
    val splitPaneState = rememberSplitPaneState(0.55f)

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalSplitPane(splitPaneState = splitPaneState, modifier = Modifier.fillMaxHeight()) {
            first(200.dp) {
                LeftPanel(controller)
            }

            second(400.dp) {
                RightPanel(controller)
            }

            styledSplitter()
        }

        TopSideLoadingIndicator(controller.subView is LoadingState.Loading)
    }
}

@Composable
private fun LeftPanel(controller: DetailInterface) {
    Column(
        modifier = Modifier.padding(top = 20.dp, start = 20.dp, bottom = 20.dp, end = (5 - 1.5).dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth()
                .background(MaterialTheme.colors.surface, MaterialTheme.shapes.medium)
                .clip(MaterialTheme.shapes.medium)
        ) {
            val scope = rememberCoroutineScope()

            val sidebarScrollState = rememberScrollState()

            val modIcon by derivedStateOf { controller.subView.flatMap { it.modIcon } }
            LaunchedEffect(controller.subView) {
                val subView = controller.subView
                if (subView is LoadingState.Loaded) {
                    subView.data.loadModIcon()
                }
            }

            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(15.dp),
                    modifier = Modifier.fillMaxHeight().weight(1f).verticalScroll(sidebarScrollState)
                        .padding(15.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        ReloadableIcon(modIcon) {
                            when (val subView = controller.subView) {
                                LoadingState.Error -> controller.reloadSubViews()
                                is LoadingState.Loaded -> scope.launch { subView.data.loadModIcon() }
                                LoadingState.Loading -> {}
                            }
                        }

                        val data = when (val subView = controller.subView) {
                            LoadingState.Error -> ModSideBarData("Error loading", "Error loading")
                            is LoadingState.Loaded -> ModSideBarData(
                                subView.data.title, subView.data.description
                            )
                            LoadingState.Loading -> ModSideBarData("Loading...", "Loading...")
                        }

                        Text(
                            data.title, style = MaterialTheme.typography.h6,
                            color = PackVulcanTheme.colors.headingColor
                        )

                        Text(data.description)
                    }

                    Divider()

                    Box(Modifier.weight(1f))
                }

                VerticalScrollbar(
                    adapter = rememberScrollbarAdapter(sidebarScrollState),
                    modifier = Modifier.padding(vertical = 15.dp)
                )
            }
        }

        SmallTextButton(onClick = { controller.reloadSubViews() }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Refresh, "reload")

            Text("Reload", modifier = Modifier.padding(start = 10.dp))
        }
    }
}

private data class ModSideBarData(val title: String, val description: String)

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun RightPanel(controller: DetailInterface) {
    val subView = controller.subView

    Box(modifier = Modifier.padding(top = 20.dp, start = (5 - 1.5).dp, bottom = 20.dp, end = 20.dp)) {
        Column(
            modifier = Modifier.fillMaxSize()
                .background(MaterialTheme.colors.surface, MaterialTheme.shapes.medium)
                .clip(MaterialTheme.shapes.medium)
        ) {
            TabRow(
                selectedTabIndex = controller.curTab.ordinal,
            ) {
                for (tab in ViewType.values()) {
                    val enabled = if (subView is LoadingState.Loaded) {
                        when (tab) {
                            ViewType.BODY -> true
                            ViewType.GALLERY -> subView.data.supportsGallery
                            ViewType.VERSIONS -> subView.data.supportsVersions
                        }
                    } else true

                    Tab(
                        selected = controller.curTab == tab,
                        onClick = { controller.setCurTab(tab) },
                        text = { Text(text = tab.prettyName) },
                        enabled = enabled
                    )
                }
            }

            AnimatedContent(
                targetState = controller.curTab,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                transitionSpec = AnimatedContentScope<ViewType>::slidingTransitionSpec
            ) { tab ->
                when (tab) {
                    ViewType.BODY -> {
                        if (subView is LoadingState.Loaded) {
                            subView.data.doBody()
                        }
                    }
                    ViewType.GALLERY -> {
                        if (subView is LoadingState.Loaded && subView.data.supportsGallery) {
                            subView.data.doGallery()
                        }
                    }
                    ViewType.VERSIONS -> {
                        if (subView is LoadingState.Loaded && subView.data.supportsVersions) {
                            subView.data.doVersions()
                        }
                    }
                }
            }
        }
    }
}
