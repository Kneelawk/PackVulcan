package com.kneelawk.packvulcan.ui.util.dialog.file

import java.nio.file.Path

fun interface FileVisibilityFilter {
    companion object {
        val ACCEPT_ALL = FileVisibilityFilter { true }
    }

    fun accept(path: Path): Boolean
}