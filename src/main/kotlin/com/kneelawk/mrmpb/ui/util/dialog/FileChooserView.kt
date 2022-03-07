package com.kneelawk.mrmpb.ui.util.dialog

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.awt.awtEvent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kneelawk.mrmpb.GlobalSettings
import com.kneelawk.mrmpb.ui.theme.MrMpBIcons
import com.kneelawk.mrmpb.ui.theme.MrMpBTheme
import com.kneelawk.mrmpb.ui.util.ContainerBox
import kotlinx.coroutines.launch
import java.awt.event.MouseEvent
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.io.path.pathString

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FileChooserView(controller: FileChooserInterface) {
    if (controller.showCreateFolderDialog) {
        CreateFolderDialog(controller.newCreateFolderController())
    }

    Column(
        modifier = Modifier.padding(20.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val selectedError = controller.selectedError

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
                controller.setViewingHome()
            }) {
                Icon(Icons.Default.Home, "go home")
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

                    TextButton(onClick = {
                        controller.selectedUpdate(path)
                    }, modifier = Modifier.fillMaxWidth().onPointerEvent(PointerEventType.Press) {
                        if (it.awtEvent.button == MouseEvent.BUTTON1 && it.awtEvent.clickCount == 2) {
                            controller.doubleClick(path)
                        }
                    }, colors = ButtonDefaults.textButtonColors(backgroundColor = background)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                when (element.type) {
                                    FileChooserInterface.FileListElementType.FILE -> MrMpBIcons.file
                                    FileChooserInterface.FileListElementType.FOLDER -> MrMpBIcons.folder
                                }, "file"
                            )
                            Text(path.name, modifier = Modifier.padding(start = 10.dp))
                        }
                    }
                }
            }

            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(controller.listState), modifier = Modifier.fillMaxHeight()
            )
        }

        TextField(value = controller.selected, onValueChange = {
            controller.selectedFieldUpdate(it)
        }, modifier = Modifier.fillMaxWidth(), isError = selectedError != null)

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
private fun ParentSelector(fullPath: Path, selectedPath: Path, pathSelected: (Path) -> Unit) {
    Row {
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
                PathSegment(path, selectedPath, path.pathString, pathSelected)

                for (segment in fullPath) {
                    path = path.resolve(segment)
                    PathSegment(path, selectedPath, segment.name, pathSelected)
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
private fun PathSegment(path: Path, selectedPath: Path, name: String, pathSelected: (Path) -> Unit) {
    val background = if (path == selectedPath) {
        MaterialTheme.colors.primary
    } else {
        MaterialTheme.colors.secondary
    }

    Button(
        onClick = { pathSelected(path) },
        colors = ButtonDefaults.buttonColors(backgroundColor = background)
    ) {
        Text(name)
    }
}

@Composable
private fun CreateFolderDialog(controller: CreateFolderInterface) {
    Dialog(title = "Create New Folder...", onCloseRequest = { controller.cancel() }) {
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
                        isError = folderNameError != null
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
