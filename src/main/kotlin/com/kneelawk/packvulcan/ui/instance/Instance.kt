package com.kneelawk.packvulcan.ui.instance

import androidx.compose.runtime.Composable

interface Instance {
    @Composable
    fun compose(onCloseRequest: () -> Unit)
}