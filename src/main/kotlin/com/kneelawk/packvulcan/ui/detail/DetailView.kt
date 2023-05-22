package com.kneelawk.packvulcan.ui.detail

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import com.kneelawk.packvulcan.ui.util.widgets.ModIcon
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
                Column(
                    Modifier.padding(top = 20.dp, start = 20.dp, bottom = 20.dp, end = (5 - 1.5).dp),
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
                                    ModIcon(modIcon) {
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
                                        color = PackVulcanTheme.colors.headingColor, modifier = Modifier.weight(1f)
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

            second(400.dp) {
                val subView = controller.subView
                if (subView is LoadingState.Loaded) {
                    subView.data.doBody()
                }
            }

            styledSplitter()
        }

        TopSideLoadingIndicator(controller.subView is LoadingState.Loading)
    }
}

private data class ModSideBarData(val title: String, val description: String)
