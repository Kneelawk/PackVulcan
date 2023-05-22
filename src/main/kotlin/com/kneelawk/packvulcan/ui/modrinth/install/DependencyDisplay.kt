package com.kneelawk.packvulcan.ui.modrinth.install

import androidx.compose.runtime.MutableState
import com.kneelawk.packvulcan.model.SimpleModFileInfo

data class DependencyDisplay(val mod: SimpleModFileInfo, val install: MutableState<Boolean>)
