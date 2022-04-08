package com.kneelawk.packvulcan.ui.util.dialog.file

import java.nio.file.Path

fun interface FileSelectionFilter {
    companion object {
        val ACCEPT_ALL = FileSelectionFilter { null }
    }

    suspend fun getError(path: Path): String?
}