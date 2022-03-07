package com.kneelawk.mrmpb.ui.util.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.kneelawk.mrmpb.ui.GlobalSettings
import com.kneelawk.mrmpb.ui.theme.MrMpBIcons
import com.kneelawk.mrmpb.ui.theme.MrMpBTheme
import com.kneelawk.mrmpb.ui.util.ContainerBox
import kotlinx.coroutines.launch
import java.awt.event.MouseEvent
import java.nio.file.Path
import kotlin.io.path.isDirectory
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

        LazyColumn(
            state = controller.listState, modifier = Modifier.fillMaxWidth().weight(1f)
                .background(MaterialTheme.colors.surface, MaterialTheme.shapes.small)
        ) {
            items(controller.fileList, { it.name }) { path ->
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
                            if (path.isDirectory()) {
                                MrMpBIcons.folder
                            } else {
                                MrMpBIcons.file
                            }, "file"
                        )
                        Text(path.name, modifier = Modifier.padding(start = 10.dp))
                    }
                }
            }
        }

        TextField(value = controller.selected, onValueChange = {
            controller.selectedFieldUpdate(it)
        }, modifier = Modifier.fillMaxWidth())

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

        Row(
            modifier = Modifier.weight(1F).background(MaterialTheme.colors.surface, MaterialTheme.shapes.medium)
                .horizontalScroll(segmentScrollState)
        ) {
            var path =
                fullPath.root ?: throw IllegalStateException("fullPath must be an absolute path! fullPath: $fullPath")

            // Make sure to have a path segment for the root folder
            PathSegment(path, selectedPath, path.pathString, pathSelected)

            for (segment in fullPath) {
                path = path.resolve(segment)
                PathSegment(path, selectedPath, segment.name, pathSelected)
            }
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

                    Text("New Folder Name:", style = MaterialTheme.typography.h5)
                    TextField(
                        value = controller.folderName, onValueChange = { controller.folderNameUpdate(it) },
                        modifier = Modifier.fillMaxWidth().focusRequester(requester)
                    )

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
