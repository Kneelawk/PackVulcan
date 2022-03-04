package com.kneelawk.mrmpb.ui.util

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.awtEvent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.rememberDialogState
import com.kneelawk.mrmpb.ui.GlobalSettings
import com.kneelawk.mrmpb.ui.theme.MrMpBIcons
import com.kneelawk.mrmpb.ui.theme.MrMpBTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.event.MouseEvent
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors
import kotlin.io.path.*

@Composable
fun OpenFileDialog(title: String, finished: (Path?) -> Unit) {
//    AwtWindow(
//        create = {
//            object : FileDialog(null as? Frame, title, LOAD) {
//                override fun setVisible(b: Boolean) {
//                    super.setVisible(b)
//                    if (b) {
//                        finished(file)
//                    }
//                }
//            }
//        }, dispose = FileDialog::dispose
//    )

    FileChooser(title, FileChooserMode.OPEN_FILE, finished)
}

@Composable
fun OpenDirectoryDialog(title: String, finished: (Path?) -> Unit) {
    FileChooser(title, FileChooserMode.OPEN_DIRECTORY, finished)
}

enum class FileChooserMode {
    SAVE, OPEN_FILE, OPEN_DIRECTORY
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FileChooser(title: String, mode: FileChooserMode, finished: (Path?) -> Unit) {
    val state = rememberDialogState(size = DpSize(1280.dp, 720.dp))

    Dialog(title = title, state = state, onCloseRequest = {
        finished(null)
    }) {
//        SwingPanel(modifier = Modifier.fillMaxSize(), factory = {
//            JFileChooser().apply {
//                SwingUtilities.updateComponentTreeUI(this)
//                fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
//                addActionListener {
//                    if (it.actionCommand == JFileChooser.APPROVE_SELECTION) {
//                        finished(selectedFile)
//                    } else if (it.actionCommand == JFileChooser.CANCEL_SELECTION) {
//                        finished(null)
//                    }
//                }
//            }
//        })

        val coroutineScope = rememberCoroutineScope()
        var viewing by remember { mutableStateOf(Paths.get(System.getProperty("user.home"))) }
        var oldViewing by remember { mutableStateOf(viewing) }
        var selected by remember { mutableStateOf("") }
        val fileList = remember { mutableStateListOf<Path>() }
        val listState = rememberLazyListState()
        var refreshFileList by remember { mutableStateOf(false) }
        var showHiddenFiles by remember { mutableStateOf(false) }

        var showCreateFolder by remember { mutableStateOf(false) }

        LaunchedEffect(viewing, refreshFileList, showHiddenFiles) {
            fileList.clear()
            val list = withContext(Dispatchers.IO) {
                Files.list(viewing)
            }.filter {
                (it.isDirectory() || mode != FileChooserMode.OPEN_DIRECTORY) && (!it.isHidden() || showHiddenFiles)
            }.sorted { o1, o2 -> o1.name.compareTo(o2.name) }.collect(Collectors.toList())
            fileList.addAll(list)

            val index = fileList.indexOf(Paths.get(selected))
            if (index > 0) {
                listState.scrollToItem(index)
            } else {
                listState.scrollToItem(0)
            }
        }

        if (showCreateFolder) {
            CreateFolder {
                showCreateFolder = false
                it?.let { dirName ->
                    coroutineScope.launch(Dispatchers.IO) {
                        val newDir = viewing.resolve(dirName)
                        Files.createDirectory(newDir)
                        withContext(Dispatchers.Main) {
                            selected = newDir.pathString
                            // toggle to cause a refresh :/
                            refreshFileList = !refreshFileList
                        }
                    }
                }
            }
        }

        MrMpBTheme(GlobalSettings.darkMode) {
            ContainerBox {
                Column(
                    modifier = Modifier.padding(20.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = {
                            showHiddenFiles = !showHiddenFiles
                        }) {
                            val shown = if (showHiddenFiles) {
                                "Shown"
                            } else {
                                "Hidden"
                            }
                            Text("Hidden Files: $shown")
                        }

                        IconButton(onClick = {
                            viewing = Paths.get(System.getProperty("user.home"))
                        }) {
                            Icon(Icons.Default.Home, "go home")
                        }

                        IconButton(onClick = {
                            showCreateFolder = true
                        }) {
                            Icon(MrMpBIcons.create_new_folder, "create new folder")
                        }
                    }

                    ParentSelector(oldViewing, viewing) {
                        viewing = it
                    }

                    LazyColumn(
                        state = listState, modifier = Modifier.fillMaxWidth().weight(1f)
                            .background(MaterialTheme.colors.surface, MaterialTheme.shapes.small)
                    ) {
                        items(fileList) { path ->
                            TextButton(onClick = {
                                selected = path.pathString
                            }, modifier = Modifier.fillMaxWidth().onPointerEvent(PointerEventType.Press) {
                                if (it.awtEvent.button == MouseEvent.BUTTON1 && it.awtEvent.clickCount == 2) {
                                    if (path.isDirectory()) {
                                        viewing = path
                                        oldViewing = path
                                    }
                                }
                            }, colors = ButtonDefaults.textButtonColors(
                                backgroundColor = if (path == Paths.get(selected)) {
                                    MaterialTheme.colors.secondary
                                } else {
                                    Color.Transparent
                                }
                            )
                            ) {
                                Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.fillMaxWidth()) {
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

                    TextField(value = selected, onValueChange = {
                        if (isValidFilename(it)) {
                            selected = it
                        }
                    }, modifier = Modifier.fillMaxWidth())

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = {
                            finished(null)
                        }, colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)) {
                            Icon(Icons.Default.Close, "cancel")
                            Text("Cancel", modifier = Modifier.padding(start = 5.dp))
                        }

                        val selectedValid = isValidFilename(selected) && when (mode) {
                            FileChooserMode.SAVE -> selected.isNotEmpty()
                            FileChooserMode.OPEN_FILE -> selected.isNotEmpty() && Paths.get(selected).isRegularFile()
                            FileChooserMode.OPEN_DIRECTORY -> selected.isNotEmpty() && Paths.get(selected).isDirectory()
                        }

                        Button(onClick = {
                            if (selectedValid) {
                                finished(Paths.get(selected))
                            }
                        }, enabled = selectedValid) {
                            Icon(Icons.Default.Check, "select")
                            Text("Select", modifier = Modifier.padding(start = 5.dp))
                        }
                    }
                }
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
            modifier = Modifier.weight(1F).background(MaterialTheme.colors.surface, MaterialTheme.shapes.small)
                .horizontalScroll(segmentScrollState)
        ) {
            var path = fullPath.root
            for (segment in fullPath) {
                path = path.resolve(segment)
                val curPath = path

                val background = if (curPath == selectedPath) {
                    MaterialTheme.colors.primary
                } else {
                    MaterialTheme.colors.secondary
                }

                Button(
                    onClick = { pathSelected(curPath) },
                    colors = ButtonDefaults.buttonColors(backgroundColor = background)
                ) {
                    Text(segment.name)
                }
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
fun CreateFolder(finished: (String?) -> Unit) {
    Dialog(title = "Create New Folder...", onCloseRequest = { finished(null) }) {
        MrMpBTheme(darkTheme = GlobalSettings.darkMode) {
            ContainerBox {
                Column(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    var folderName by remember { mutableStateOf("") }
                    val requester = remember { FocusRequester() }

                    Text("New Folder Name:", style = MaterialTheme.typography.h5)
                    TextField(
                        value = folderName, onValueChange = { folderName = it },
                        modifier = Modifier.fillMaxWidth().focusRequester(requester)
                    )

                    Box(Modifier.weight(1f))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = {
                            finished(null)
                        }, colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)) {
                            Icon(Icons.Default.Close, "cancel")
                            Text("Cancel", modifier = Modifier.padding(start = 5.dp))
                        }

                        val valid = isValidFolderName(folderName)

                        Button(onClick = {
                            if (valid) {
                                finished(folderName)
                            }
                        }, enabled = valid) {
                            Icon(MrMpBIcons.create_new_folder, "create")
                            Text("Create", modifier = Modifier.padding(start = 5.dp))
                        }
                    }

                    LaunchedEffect(Unit) {
                        requester.requestFocus()
                    }
                }
            }
        }
    }
}

private val invalidPathChars = charArrayOf('"', '*', ':', '<', '>', '?', '|', 0x7F.toChar())
private val invalidNameChars = charArrayOf('/', '\\')

fun isValidFilename(name: String): Boolean {
    return name.isNotBlank() && name.length <= 255 && invalidPathChars.none { name.contains(it) }
}

fun isValidFolderName(name: String): Boolean {
    return isValidFilename(name) && invalidNameChars.none { name.contains(it) }
}
