package com.kneelawk.packvulcan.ui.util.dialog.file

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.awtEventOrNull
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.rememberDialogState
import com.kneelawk.packvulcan.GlobalSettings
import com.kneelawk.packvulcan.ui.theme.PackVulcanIcons
import com.kneelawk.packvulcan.ui.theme.PackVulcanTheme
import com.kneelawk.packvulcan.ui.util.layout.DialogContainerBox
import com.kneelawk.packvulcan.ui.util.layout.VerticalScrollWrapper
import com.kneelawk.packvulcan.ui.util.widgets.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import java.awt.event.MouseEvent
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.io.path.pathString

@OptIn(ExperimentalComposeUiApi::class, ExperimentalSplitPaneApi::class, ExperimentalFoundationApi::class)
@Composable
fun FileChooserView(controller: FileChooserInterface) {
    if (controller.showCreateFolderDialog) {
        CreateFolderDialog(controller.newCreateFolderController())
    }

    Column(
        modifier = Modifier.padding(20.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val splitPaneState = rememberSplitPaneState(0.5f)

        HorizontalSplitPane(splitPaneState = splitPaneState, modifier = Modifier.weight(1f)) {
            first(150.dp) {
                val sideBarListState = rememberLazyListState()

                VerticalScrollWrapper(
                    modifier = Modifier.fillMaxHeight().padding(end = (5 - 1.5).dp),
                    adapter = rememberScrollbarAdapter(sideBarListState)
                ) {
                    LazyColumn(
                        state = sideBarListState, modifier = Modifier.fillMaxHeight().weight(1f)
                            .background(MaterialTheme.colors.surface, MaterialTheme.shapes.medium)
                    ) {
                        items(controller.homeFolderList) { type ->
                            ListButton(
                                onClick = {
                                    controller.homeFolderSelect(type)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                icon = { HomeFolderIcon(type) },
                                text = type.displayName
                            )
                        }

                        item {
                            Divider(modifier = Modifier.fillMaxWidth())
                        }

                        item {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                ListButton(
                                    onClick = { controller.addFavorite() },
                                    modifier = Modifier.weight(1f),
                                    enabled = controller.favoritesAddEnabled,
                                    icon = { Icon(Icons.Default.Add, "add") },
                                    text = "Add Favorite"
                                )

                                TooltipArea(tooltip = {
                                    Surface(
                                        modifier = Modifier.shadow(4.dp),
                                        color = MaterialTheme.colors.surface,
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text("Edit Favorites", modifier = Modifier.padding(10.dp))
                                    }
                                }) {
                                    SmallIconButton(onClick = { controller.editFavorites() }) {
                                        Icon(Icons.Default.Edit, "edit favorites")
                                    }
                                }
                            }
                        }

                        items(controller.favoritesList, { it }) { favorite ->
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                TooltipArea(
                                    tooltip = {
                                        Surface(
                                            modifier = Modifier.shadow(4.dp),
                                            color = MaterialTheme.colors.surface,
                                            shape = MaterialTheme.shapes.small
                                        ) {
                                            Text(favorite.pathString, modifier = Modifier.padding(10.dp))
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    ListButton(
                                        onClick = { controller.favoriteSelect(favorite) },
                                        modifier = Modifier.fillMaxWidth(),
                                        icon = { Icon(PackVulcanIcons.folder, "favorite") },
                                        text = favorite.name
                                    )
                                }

                                AnimatedVisibility(visible = controller.favoritesEditEnabled) {
                                    Row {
                                        TooltipArea(tooltip = {
                                            Surface(
                                                modifier = Modifier.shadow(4.dp),
                                                color = MaterialTheme.colors.surface,
                                                shape = MaterialTheme.shapes.small
                                            ) {
                                                Text("Move Favorite Up", modifier = Modifier.padding(10.dp))
                                            }
                                        }) {
                                            SmallIconButton(onClick = { controller.moveFavoriteUp(favorite) }) {
                                                Icon(Icons.Default.KeyboardArrowUp, "move up")
                                            }
                                        }

                                        TooltipArea(tooltip = {
                                            Surface(
                                                modifier = Modifier.shadow(4.dp),
                                                color = MaterialTheme.colors.surface,
                                                shape = MaterialTheme.shapes.small
                                            ) {
                                                Text("Move Favorite Down", modifier = Modifier.padding(10.dp))
                                            }
                                        }) {
                                            SmallIconButton(onClick = { controller.moveFavoriteDown(favorite) }) {
                                                Icon(Icons.Default.KeyboardArrowDown, "move down")
                                            }
                                        }

                                        TooltipArea(tooltip = {
                                            Surface(
                                                modifier = Modifier.shadow(4.dp),
                                                color = MaterialTheme.colors.surface,
                                                shape = MaterialTheme.shapes.small
                                            ) {
                                                Text("Remove Favorite", modifier = Modifier.padding(10.dp))
                                            }
                                        }) {
                                            SmallIconButton(onClick = { controller.removeFavorite(favorite) }) {
                                                Icon(Icons.Default.Close, "remove")
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Divider(modifier = Modifier.fillMaxWidth())
                        }

                        items(controller.driveList, { it.path }) { drive ->
                            ListButton(
                                onClick = {
                                    controller.driveSelect(drive.path)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                icon = { Icon(PackVulcanIcons.storage, "drive") },
                                text = drive.displayName
                            )
                        }
                    }
                }
            }

            second(400.dp) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(start = (5 - 1.5).dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SmallTextButton(onClick = {
                            controller.showHiddenFilesToggle()
                        }) {
                            val shown = if (controller.showHiddenFiles) {
                                "Shown"
                            } else {
                                "Hidden"
                            }
                            Text("Hidden Files: $shown")
                        }

                        SmallIconButton(onClick = {
                            controller.openCreateFolderDialog()
                        }) {
                            Icon(PackVulcanIcons.createNewFolder, "create new folder")
                        }
                    }

                    ParentSelector(controller.topBarViewing, controller.viewing) {
                        controller.topBarSelect(it)
                    }

                    VerticalScrollWrapper(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        adapter = rememberScrollbarAdapter(controller.listState)
                    ) {
                        LazyColumn(
                            state = controller.listState, modifier = Modifier.weight(1f).fillMaxHeight()
                        ) {
                            items(controller.fileList, { it.path.name }) { element ->
                                val path = element.path

                                val background = if (controller.isPathSelected(path)) {
                                    MaterialTheme.colors.secondary
                                } else {
                                    Color.Transparent
                                }

                                ListButton(
                                    onClick = {
                                        controller.selectedUpdate(path)
                                    },
                                    modifier = Modifier.fillMaxWidth().onPointerEvent(PointerEventType.Release) {
                                        if (it.awtEventOrNull?.button == MouseEvent.BUTTON1 && it.awtEventOrNull?.clickCount == 2) {
                                            controller.doubleClick(path)
                                        }
                                    },
                                    colors = ButtonDefaults.textButtonColors(backgroundColor = background),
                                    icon = {
                                        Icon(
                                            when (element.type) {
                                                FileListItemType.FILE -> PackVulcanIcons.file
                                                FileListItemType.FOLDER -> PackVulcanIcons.folder
                                            }, "file"
                                        )
                                    },
                                    text = path.name
                                )
                            }
                        }
                    }
                }
            }

            styledSplitter()
        }

        val selectedError = controller.selectedError

        SmallTextField(
            value = controller.selected, onValueChange = { controller.selectedFieldUpdate(it) },
            modifier = Modifier.fillMaxWidth(), isError = selectedError != null, singleLine = true
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (selectedError != null) {
                Text(selectedError)
            }

            Box(Modifier.weight(1f))

            DialogButtonBar(
                onCancel = { controller.cancel() },
                onConfirm = { controller.select() },
                confirmEnabled = controller.selectedValid
            )
        }
    }
}

@Composable
private fun HomeFolderIcon(item: HomeFolderItem) {
    when (item) {
        HomeFolderItem.HOME -> Icon(Icons.Default.Home, "home")
        HomeFolderItem.DESKTOP -> Icon(PackVulcanIcons.desktop, "desktop")
        HomeFolderItem.DOCUMENTS -> Icon(PackVulcanIcons.file, "documents")
        HomeFolderItem.DOWNLOADS -> Icon(PackVulcanIcons.download, "downloads")
        HomeFolderItem.MUSIC -> Icon(PackVulcanIcons.music, "music")
        HomeFolderItem.PICTURES -> Icon(PackVulcanIcons.image, "pictures")
        HomeFolderItem.VIDEOS -> Icon(PackVulcanIcons.movie, "videos")
    }
}

@Composable
private fun ParentSelector(fullPath: Path, selectedPath: Path, pathSelected: (Path) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        val coroutineScope = rememberCoroutineScope()
        val segmentScrollState = rememberScrollState()

        SmallIconButton(onClick = {
            coroutineScope.launch {
                segmentScrollState.scroll {
                    scrollBy(-50F)
                }
            }
        }) {
            Icon(Icons.Default.KeyboardArrowLeft, "scroll current path left")
        }

        Column(modifier = Modifier.weight(1F)) {
            Row(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colors.surface, MaterialTheme.shapes.medium)
                    .clip(MaterialTheme.shapes.medium).horizontalScroll(segmentScrollState)
            ) {
                var path =
                    fullPath.root ?: throw IllegalStateException(
                        "fullPath must be an absolute path! fullPath: $fullPath"
                    )

                // Make sure to have a path segment for the root folder
                PathSegment(path, selectedPath, path.pathString, true, fullPath == path, pathSelected)

                for (segment in fullPath) {
                    path = path.resolve(segment)
                    PathSegment(path, selectedPath, segment.name, false, fullPath == path, pathSelected)
                }
            }

            HorizontalScrollbar(
                adapter = rememberScrollbarAdapter(segmentScrollState),
                modifier = Modifier.fillMaxWidth()
            )
        }

        SmallIconButton(onClick = {
            coroutineScope.launch {
                segmentScrollState.scroll {
                    scrollBy(50F)
                }
            }
        }) {
            Icon(Icons.Default.KeyboardArrowRight, "scroll current path right")
        }
    }
}

@Composable
private fun PathSegment(
    path: Path, selectedPath: Path, name: String, isFirst: Boolean, isLast: Boolean, pathSelected: (Path) -> Unit
) {
    val background = if (path == selectedPath) {
        MaterialTheme.colors.primary
    } else {
        MaterialTheme.colors.secondary
    }

    var buttonShape = MaterialTheme.shapes.small

    if (!isFirst) {
        buttonShape = buttonShape.copy(topStart = CornerSize(0.dp), bottomStart = CornerSize(0.dp))
    }

    if (!isLast) {
        buttonShape = buttonShape.copy(topEnd = CornerSize(0.dp), bottomEnd = CornerSize(0.dp))
    }

    SmallButton(
        onClick = { pathSelected(path) },
        colors = ButtonDefaults.buttonColors(backgroundColor = background),
        shape = buttonShape
    ) {
        Text(name)
    }
}

@Composable
private fun CreateFolderDialog(controller: CreateFolderInterface) {
    Dialog(
        title = "Create New Folder...", onCloseRequest = { controller.cancel() },
        state = rememberDialogState(size = DpSize(500.dp, 350.dp))
    ) {
        PackVulcanTheme(darkTheme = GlobalSettings.darkMode) {
            DialogContainerBox {
                Column(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val requester = remember { FocusRequester() }
                    val folderNameError = controller.folderNameError

                    Text("New Folder Name:", style = MaterialTheme.typography.h5)
                    SmallTextField(
                        value = controller.folderName, onValueChange = { controller.folderNameUpdate(it) },
                        modifier = Modifier.fillMaxWidth().focusRequester(requester),
                        isError = folderNameError != null,
                        singleLine = true
                    )

                    if (folderNameError != null) {
                        Text(folderNameError)
                    }

                    Box(Modifier.weight(1f))

                    DialogButtonBar(
                        onCancel = { controller.cancel() },
                        onConfirm = { controller.createFolder() },
                        modifier = Modifier.fillMaxWidth(),
                        confirmEnabled = controller.folderNameValid
                    ) {
                        Icon(PackVulcanIcons.createNewFolder, "create")
                        Text("Create", modifier = Modifier.padding(start = 5.dp))
                    }

                    SideEffect {
                        requester.requestFocus()
                    }
                }
            }
        }
    }
}
