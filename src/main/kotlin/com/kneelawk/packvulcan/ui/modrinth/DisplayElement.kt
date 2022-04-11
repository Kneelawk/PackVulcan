package com.kneelawk.packvulcan.ui.modrinth

import androidx.compose.runtime.Composable

interface DisplayElement {
    val prettyName: String
    val icon: @Composable () -> Unit
}