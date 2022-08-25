package com.kneelawk.packvulcan.ui

import java.nio.file.Path

sealed class RootInitialState {
    object None : RootInitialState()

    object CreateNew : RootInitialState()

    data class Open(val path: Path) : RootInitialState()
}
