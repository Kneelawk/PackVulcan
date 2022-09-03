package com.kneelawk.packvulcan.ui.modrinth.install

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape

interface InstallInterface {
    val enabled: Boolean
    val loading: Boolean
    val buttonShape: Shape
    val buttonModifier: Modifier
    val loadingText: String
    val content: @Composable RowScope.() -> Unit
    val dialogOpen: Boolean
    val modName: String
    val modVersion: String
    val collectedDependencies: List<DependencyDisplay>

    fun startInstall()

    fun cancelDialog()

    fun install()
}
