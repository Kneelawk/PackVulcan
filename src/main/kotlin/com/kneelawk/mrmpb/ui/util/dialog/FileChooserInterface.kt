package com.kneelawk.mrmpb.ui.util.dialog

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import java.nio.file.Path

interface FileChooserInterface {
    val viewing: Path
    val topBarViewing: Path
    val fileList: List<FileListElement>
    val selected: String
    val selectedValid: Boolean
    val selectedError: String?
    val showHiddenFiles: Boolean

    val listState: LazyListState

    val showCreateFolderDialog: Boolean

    fun showHiddenFilesToggle()

    fun setViewingHome()

    fun openCreateFolderDialog()

    @Composable
    fun newCreateFolderController(): CreateFolderInterface

    fun topBarSelect(newViewing: Path)

    fun selectedUpdate(path: Path)

    fun doubleClick(path: Path)

    fun isPathSelected(path: Path): Boolean

    fun selectedFieldUpdate(newText: String)

    fun select()

    fun cancel()

    data class FileListElement(val path: Path, val type: FileListElementType)
    enum class FileListElementType {
        FILE, FOLDER;
    }
}
