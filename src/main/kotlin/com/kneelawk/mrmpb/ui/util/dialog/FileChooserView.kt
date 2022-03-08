package com.kneelawk.mrmpb.ui.util.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.rememberDialogState
import com.kneelawk.mrmpb.GlobalSettings
import com.kneelawk.mrmpb.ui.theme.MrMpBIcons
import com.kneelawk.mrmpb.ui.theme.MrMpBTheme
import com.kneelawk.mrmpb.ui.util.ContainerBox
import com.kneelawk.mrmpb.ui.util.ListButton
import kotlinx.coroutines.launch
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import java.awt.Cursor
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
                Row(
                    modifier = Modifier.fillMaxHeight().padding(end = (5 - 1.5).dp)
                        .background(MaterialTheme.colors.surface, MaterialTheme.shapes.medium)
                ) {
                    val sideBarListState = rememberLazyListState()

                    LazyColumn(state = sideBarListState, modifier = Modifier.fillMaxHeight().weight(1f)) {
                        items(controller.homeFolderList) { type ->
                            ListButton(onClick = {
                                controller.homeFolderSelect(type)
                            }, modifier = Modifier.fillMaxWidth()) {
                                HomeFolderIcon(type)

                                Text(type.displayName, modifier = Modifier.padding(start = 10.dp))
                            }
                        }

                        item {
                            Divider(modifier = Modifier.fillMaxWidth())
                        }

                        item {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                ListButton(
                                    onClick = { controller.addFavorite() }, modifier = Modifier.weight(1f),
                                    enabled = controller.favoritesAddEnabled
                                ) {
                                    Icon(Icons.Default.Add, "add")

                                    Text("Add Favorite", modifier = Modifier.padding(start = 10.dp))
                                }

                                TooltipArea(tooltip = {
                                    Surface(
                                        modifier = Modifier.shadow(4.dp),
                                        color = MaterialTheme.colors.surface,
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text("Edit Favorites", modifier = Modifier.padding(10.dp))
                                    }
                                }) {
                                    IconButton(onClick = { controller.editFavorites() }) {
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
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(MrMpBIcons.folder, "favorite")

                                        Text(favorite.name, modifier = Modifier.padding(start = 10.dp))
                                    }
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
                                            IconButton(onClick = { controller.moveFavoriteUp(favorite) }) {
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
                                            IconButton(onClick = { controller.moveFavoriteDown(favorite) }) {
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
                                            IconButton(onClick = { controller.removeFavorite(favorite) }) {
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
                            ListButton(onClick = {
                                controller.driveSelect(drive.path)
                            }, modifier = Modifier.fillMaxWidth()) {
                                Icon(MrMpBIcons.storage, "drive")

                                Text(drive.displayName, modifier = Modifier.padding(start = 10.dp))
                            }
                        }
                    }

                    VerticalScrollbar(
                        adapter = rememberScrollbarAdapter(sideBarListState), modifier = Modifier.fillMaxHeight()
                    )
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
                        TextButton(onClick = {
                            controller.showHiddenFilesToggle()
                        }) {
                            val shown = if (controller.showHiddenFiles) {
                                "Shown"
                            } else {
                                "Hidden"
                            }
                            Text("Hidden Files: $shown")
                        }

                        IconButton(onClick = {
                            controller.openCreateFolderDialog()
                        }) {
                            Icon(MrMpBIcons.create_new_folder, "create new folder")
                        }
                    }

                    ParentSelector(controller.topBarViewing, controller.viewing) {
                        controller.topBarSelect(it)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().weight(1f)
                            .background(MaterialTheme.colors.surface, MaterialTheme.shapes.medium)
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

                                ListButton(onClick = {
                                    controller.selectedUpdate(path)
                                }, modifier = Modifier.fillMaxWidth().onPointerEvent(PointerEventType.Press) {
                                    if (it.awtEventOrNull?.button == MouseEvent.BUTTON1 && it.awtEventOrNull?.clickCount == 2) {
                                        controller.doubleClick(path)
                                    }
                                }, colors = ButtonDefaults.textButtonColors(backgroundColor = background)
                                ) {
                                    Icon(
                                        when (element.type) {
                                            FileListItemType.FILE -> MrMpBIcons.file
                                            FileListItemType.FOLDER -> MrMpBIcons.folder
                                        }, "file"
                                    )
                                    Text(path.name, modifier = Modifier.padding(start = 10.dp))
                                }
                            }
                        }

                        VerticalScrollbar(
                            adapter = rememberScrollbarAdapter(controller.listState),
                            modifier = Modifier.fillMaxHeight()
                        )
                    }
                }
            }

            splitter {
                visiblePart {
                    Column(verticalArrangement = Arrangement.Center) {
                        Box(
                            Modifier
                                .width(3.dp)
                                .height(30.dp)
                                .background(MaterialTheme.colors.surface, shape = MaterialTheme.shapes.small)
                        )
                    }
                }
                handle {
                    Box(
                        Modifier
                            .markAsHandle()
                            .pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))
                            .width(10.dp)
                            .fillMaxHeight()
                    )
                }
            }
        }

        val selectedError = controller.selectedError

        TextField(
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

            Button(onClick = {
                controller.cancel()
            }, colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)) {
                Icon(Icons.Default.Close, "cancel")
                Text("Cancel", modifier = Modifier.padding(start = 5.dp))
            }

            Button(onClick = {
                controller.select()
            }, enabled = controller.selectedValid) {
                Icon(Icons.Default.Check, "select")
                Text("Select", modifier = Modifier.padding(start = 5.dp))
            }
        }
    }
}

@Composable
private fun HomeFolderIcon(item: HomeFolderItem) {
    when (item) {
        HomeFolderItem.HOME -> Icon(Icons.Default.Home, "home")
        HomeFolderItem.DESKTOP -> Icon(MrMpBIcons.desktop, "desktop")
        HomeFolderItem.DOCUMENTS -> Icon(MrMpBIcons.file, "documents")
        HomeFolderItem.DOWNLOADS -> Icon(MrMpBIcons.download, "downloads")
        HomeFolderItem.MUSIC -> Icon(MrMpBIcons.music, "music")
        HomeFolderItem.PICTURES -> Icon(MrMpBIcons.image, "pictures")
        HomeFolderItem.VIDEOS -> Icon(MrMpBIcons.movie, "videos")
    }
}

@Composable
private fun ParentSelector(fullPath: Path, selectedPath: Path, pathSelected: (Path) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        val coroutineScope = rememberCoroutineScope()
        val segmentScrollState = rememberScrollState()

        IconButton(onClick = {
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
                    .horizontalScroll(segmentScrollState)
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
                adapter = rememberScrollbarAdapter(segmentScrollState), modifier = Modifier.fillMaxWidth()
            )
        }

        IconButton(onClick = {
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

    val startCorners = if (isFirst) {
        4.dp
    } else {
        0.dp
    }

    val endCorners = if (isLast) {
        4.dp
    } else {
        0.dp
    }

    val buttonShape = RoundedCornerShape(
        topStart = startCorners, bottomStart = startCorners, topEnd = endCorners, bottomEnd = endCorners
    )

    Button(
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
        MrMpBTheme(darkTheme = GlobalSettings.darkMode) {
            ContainerBox {
                Column(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val requester = remember { FocusRequester() }
                    val folderNameError = controller.folderNameError

                    Text("New Folder Name:", style = MaterialTheme.typography.h5)
                    TextField(
                        value = controller.folderName, onValueChange = { controller.folderNameUpdate(it) },
                        modifier = Modifier.fillMaxWidth().focusRequester(requester),
                        isError = folderNameError != null,
                        singleLine = true
                    )

                    if (folderNameError != null) {
                        Text(folderNameError)
                    }

                    Box(Modifier.weight(1f))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = {
                            controller.cancel()
                        }, colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)) {
                            Icon(Icons.Default.Close, "cancel")
                            Text("Cancel", modifier = Modifier.padding(start = 5.dp))
                        }

                        Button(onClick = {
                            controller.createFolder()
                        }, enabled = controller.folderNameValid) {
                            Icon(MrMpBIcons.create_new_folder, "create")
                            Text("Create", modifier = Modifier.padding(start = 5.dp))
                        }
                    }

                    SideEffect {
                        requester.requestFocus()
                    }
                }
            }
        }
    }
}
