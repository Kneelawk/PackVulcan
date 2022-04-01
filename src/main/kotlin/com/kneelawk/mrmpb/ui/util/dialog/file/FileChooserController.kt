package com.kneelawk.mrmpb.ui.util.dialog.file

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import com.kneelawk.mrmpb.GlobalSettings
import com.kneelawk.mrmpb.util.Conflator
import kotlinx.coroutines.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.util.stream.Collectors
import kotlin.io.path.*

// A map of home folder items to their paths relative to 'user.home'
private val homeFolders = mapOf(
    HomeFolderItem.HOME to HOME_FOLDER,
    HomeFolderItem.DESKTOP to HOME_FOLDER.resolve("Desktop"),
    HomeFolderItem.DOCUMENTS to HOME_FOLDER.resolve("Documents"),
    HomeFolderItem.DOWNLOADS to HOME_FOLDER.resolve("Downloads"),
    HomeFolderItem.MUSIC to HOME_FOLDER.resolve("Music"),
    HomeFolderItem.PICTURES to HOME_FOLDER.resolve("Pictures"),
    HomeFolderItem.VIDEOS to HOME_FOLDER.resolve("Videos")
)

@Composable
fun rememberFileChooserController(
    mode: FileChooserMode, initialFolder: Path = HOME_FOLDER, initialSelection: String = "",
    chooserFilter: FileChooserFilter = FileChooserFilter.ACCEPT_ALL, finished: (Path?) -> Unit
): FileChooserInterface {
    // Keep input updated with recompositions
    @Suppress("NAME_SHADOWING")
    val mode by rememberUpdatedState(mode)

    @Suppress("NAME_SHADOWING")
    val finished by rememberUpdatedState(finished)

    // Grab the composable's coroutine scope, so we can run stuff in it later
    val composableScope = rememberCoroutineScope()

    // Setup state
    val homeFolderList = remember { mutableStateListOf<HomeFolderItem>() }
    val driveList = remember { mutableStateListOf<DriveItem>() }

    val viewingState: MutableState<Path> = remember { mutableStateOf(initialFolder) }
    val cViewing by viewingState

    val favoritesAddEnabledState = derivedStateOf { !GlobalSettings.fileChooserFavoritesList.contains(cViewing) }
    val favoritesEditEnabledState = remember { mutableStateOf(false) }

    val topBarViewingState = remember { mutableStateOf(cViewing) }

    val fileList = remember { mutableStateListOf<FileListItem>() }

    val selectedState = remember { mutableStateOf(initialSelection) }
    val cSelected by selectedState

    val selectedProduced by produceState(SelectedProduced(false, null, Paths.get("")), cSelected) {
        // we do this checking in a produceState, so that we can handle cases when filesystem checks like
        // `isRegularFile()` or `isDirectory()` take a significant amount of time.
        val selectedPath = Paths.get(cSelected)
        value = if (cSelected.isEmpty()) {
            SelectedProduced(false, null, selectedPath)
        } else if (!isValidFilename(cSelected)) {
            SelectedProduced(
                false,
                "Paths must not be blank or longer than 255 characters and must not contain '\"', '*', '<', '>', '?', '|', newline, or tab characters.",
                selectedPath
            )
        } else {
            when (mode) {
                FileChooserMode.SAVE -> {
                    SelectedProduced(true, null, selectedPath)
                }
                FileChooserMode.OPEN_FILE -> {
                    if (withContext(Dispatchers.IO) { selectedPath.isRegularFile() }) {
                        SelectedProduced(true, null, selectedPath)
                    } else {
                        SelectedProduced(false, "The selected file is not a regular file.", selectedPath)
                    }
                }
                FileChooserMode.OPEN_DIRECTORY -> {
                    if (withContext(Dispatchers.IO) { selectedPath.isDirectory() }) {
                        SelectedProduced(true, null, selectedPath)
                    } else {
                        SelectedProduced(false, "The selected file is not a folder.", selectedPath)
                    }
                }
            }
        }
    }
    val cSelectedPath by derivedStateOf { selectedProduced.selectedPath }
    val selectedValidState = derivedStateOf { selectedProduced.valid }
    val selectedErrorState = derivedStateOf { selectedProduced.error }

    val showHiddenFilesState = remember { mutableStateOf(false) }
    val cShowHiddenFiles by showHiddenFilesState

    val listState = rememberLazyListState()

    val showCreateFolderState = remember { mutableStateOf(false) }

    // Setup suspend stuff
    val recalculateFileList = remember {
        Conflator<Unit>(composableScope) {
            fileList.clear()

            val list = withContext(Dispatchers.IO) {
                // Check just in case we're handed a path that doesn't exist
                if (cViewing.exists()) {
                    // Note, we must have a `use` here, as the stream *must* be closed after it is used, and `collect`
                    // doesn't appear to do that for us.
                    Files.list(cViewing).use { stream ->
                        stream.filter {
                            (it.isDirectory() || mode != FileChooserMode.OPEN_DIRECTORY)
                                    && (!it.isHidden() || cShowHiddenFiles)
                                    && chooserFilter.accept(it)
                        }.sorted { o1, o2 -> o1.name.compareTo(o2.name, ignoreCase = true) }.map {
                            val type = when {
                                it.isDirectory() -> FileListItemType.FOLDER
                                else -> FileListItemType.FILE
                            }

                            FileListItem(it, type)
                        }.collect(Collectors.toList())
                    }
                } else {
                    listOf()
                }
            }

            fileList.addAll(list)

            val index = fileList.indexOfFirst { it.path == cSelectedPath }
            if (index > 0) {
                listState.scrollToItem(index)
            } else {
                listState.scrollToItem(0)
            }
        }
    }

    // Find the home folders
    LaunchedEffect(Unit) {
        for ((key, path) in homeFolders) {
            if (withContext(Dispatchers.IO) { path.exists() }) {
                homeFolderList.add(key)
            }
        }
    }

    // Refresh drives
    LaunchedEffect(Unit) {
        while (isActive) {
            val newDriveList = DriveDetector.detectDrives()
            driveList.clear()
            driveList.addAll(newDriveList)

            delay(5000)
        }
    }

    // Calculate everything the first time or when stuff updates
    LaunchedEffect(mode, cViewing, cShowHiddenFiles) {
        // Note that `cSelected`/`cSelectedPath` is not one of the input variables, because we don't want to
        // re-calculate every time a new element is selected.
        recalculateFileList.send(Unit)
    }

    // No need to remember this as all its state is remembered up above
    return object : FileChooserInterface {
        override var viewing by viewingState
        override var topBarViewing by topBarViewingState
        override val fileList = fileList
        override var selected by selectedState
        override val selectedValid by selectedValidState
        override val selectedError by selectedErrorState
        override var showHiddenFiles by showHiddenFilesState
        override val homeFolderList = homeFolderList
        override val driveList = driveList
        override val favoritesAddEnabled by favoritesAddEnabledState
        override var favoritesEditEnabled by favoritesEditEnabledState
        override val favoritesList = GlobalSettings.fileChooserFavoritesList
        override val listState = listState
        override var showCreateFolderDialog by showCreateFolderState

        override fun homeFolderSelect(item: HomeFolderItem) {
            val newViewing =
                homeFolders[item] ?: throw IllegalStateException("Encountered unimplemented home folder type: $item")
            viewing = newViewing

            if (!topBarViewing.startsWith(newViewing)) {
                topBarViewing = newViewing
            }
        }

        override fun driveSelect(path: Path) {
            composableScope.launch {
                if (withContext(Dispatchers.IO) { path.exists() }) {
                    viewing = path

                    if (!topBarViewing.startsWith(path)) {
                        topBarViewing = path
                    }
                }
            }
        }

        override fun addFavorite() {
            GlobalSettings.fileChooserFavoritesList.add(cViewing)
        }

        override fun editFavorites() {
            favoritesEditEnabled = !favoritesEditEnabled
        }

        override fun removeFavorite(path: Path) {
            GlobalSettings.fileChooserFavoritesList.remove(path)
        }

        override fun moveFavoriteUp(path: Path) {
            val index = GlobalSettings.fileChooserFavoritesList.indexOf(path)
            if (index > 0) {
                val replaced = GlobalSettings.fileChooserFavoritesList.set(index - 1, path)
                GlobalSettings.fileChooserFavoritesList[index] = replaced
            }
        }

        override fun moveFavoriteDown(path: Path) {
            val index = GlobalSettings.fileChooserFavoritesList.indexOf(path)
            if (index < GlobalSettings.fileChooserFavoritesList.size - 1) {
                val replaced = GlobalSettings.fileChooserFavoritesList.set(index + 1, path)
                GlobalSettings.fileChooserFavoritesList[index] = replaced
            }
        }

        override fun favoriteSelect(path: Path) {
            composableScope.launch {
                if (withContext(Dispatchers.IO) { path.exists() }) {
                    viewing = path

                    if (!topBarViewing.startsWith(path)) {
                        topBarViewing = path
                    }
                }
            }
        }

        override fun showHiddenFilesToggle() {
            showHiddenFiles = !showHiddenFiles
        }

        override fun openCreateFolderDialog() {
            showCreateFolderDialog = true
        }

        @Composable
        override fun newCreateFolderController(): CreateFolderInterface {
            val curViewing = remember { viewing }

            val folderNameState = remember { mutableStateOf("") }
            val cFolderName by folderNameState
            val folderNameProduced by produceState(
                FolderNameProduced(false, null, curViewing.resolve("")), cFolderName
            ) {
                val newFolder = curViewing.resolve(cFolderName)
                value = if (cFolderName.isEmpty()) {
                    FolderNameProduced(false, null, newFolder)
                } else if (!isValidFolderName(cFolderName)) {
                    FolderNameProduced(
                        false,
                        "Folder names must not be blank or longer than 255 characters and must not contain '\"', '*', '<', '>', '?', '|', ':', '/', '\\', newline, or tab characters.",
                        newFolder
                    )
                } else if (withContext(Dispatchers.IO) { newFolder.exists() }) {
                    FolderNameProduced(false, "\"$cFolderName\" already exists.", newFolder)
                } else {
                    FolderNameProduced(true, null, newFolder)
                }
            }
            val folderNameValidState = derivedStateOf { folderNameProduced.valid }
            val folderNameErrorState = derivedStateOf { folderNameProduced.error }
            val newFolder by derivedStateOf { folderNameProduced.newFolder }

            // No need to remember this as all its state is remembered elsewhere
            return object : CreateFolderInterface {
                override var folderName by folderNameState
                override val folderNameValid by folderNameValidState
                override val folderNameError by folderNameErrorState

                override fun folderNameUpdate(newName: String) {
                    folderName = newName
                }

                override fun createFolder() {
                    if (folderNameValid) {
                        showCreateFolderDialog = false
                        composableScope.launch {
                            withContext(Dispatchers.IO) {
                                Files.createDirectory(newFolder)
                            }
                            selected = newFolder.pathString

                            recalculateFileList.send(Unit)
                        }
                    }
                }

                override fun cancel() {
                    showCreateFolderDialog = false
                }
            }
        }

        override fun topBarSelect(newViewing: Path) {
            viewing = newViewing
        }

        override fun selectedUpdate(path: Path) {
            selected = path.pathString
        }

        override fun doubleClick(path: Path) {
            composableScope.launch {
                if (withContext(Dispatchers.IO) { path.isReadable() }) {
                    val attributes = withContext(Dispatchers.IO) { path.readAttributes<BasicFileAttributes>() }
                    if (attributes.isDirectory) {
                        viewing = path
                        topBarViewing = path
                    } else if (
                        attributes.isRegularFile
                        && mode == FileChooserMode.OPEN_FILE
                        && chooserFilter.accept(path)
                    ) {
                        finished(path)
                    }
                }
            }
        }

        override fun isPathSelected(path: Path): Boolean {
            return path == cSelectedPath
        }

        override fun selectedFieldUpdate(newText: String) {
            selected = newText
        }

        override fun select() {
            if (selectedValid) {
                finished(cSelectedPath)
            }
        }

        override fun cancel() {
            finished(null)
        }
    }
}

private val invalidPathChars = charArrayOf('"', '*', '<', '>', '?', '|', 0x7F.toChar(), '\n', '\t')
private val invalidNameChars = charArrayOf('/', '\\', ':')

private fun isValidFilename(name: String): Boolean {
    return name.isNotBlank() && name.length <= 255 && invalidPathChars.none { name.contains(it) }
}

private fun isValidFolderName(name: String): Boolean {
    return isValidFilename(name) && invalidNameChars.none { name.contains(it) }
}

private data class FolderNameProduced(val valid: Boolean, val error: String?, val newFolder: Path)
private data class SelectedProduced(val valid: Boolean, val error: String?, val selectedPath: Path)
