package com.kneelawk.packvulcan.ui.modrinth.install

import androidx.compose.runtime.MutableState
import com.kneelawk.packvulcan.model.SimpleModInfo

data class DependencyDisplay(val mod: SimpleModInfo, val install: MutableState<Boolean>)
