package com.kneelawk.packvulcan.ui.util.dialog.file

import java.nio.file.Path

fun interface FileChooserFilter {
    companion object {
        val ACCEPT_ALL = FileChooserFilter { true }
    }

    fun accept(path: Path): Boolean
}