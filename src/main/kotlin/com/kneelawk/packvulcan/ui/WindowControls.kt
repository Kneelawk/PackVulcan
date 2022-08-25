package com.kneelawk.packvulcan.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class WindowControls(title: String) {
    var title by mutableStateOf(title)
}
