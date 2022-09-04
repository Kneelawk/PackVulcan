package com.kneelawk.packvulcan.ui.modrinth.install

import androidx.compose.runtime.Composable
import com.kneelawk.packvulcan.engine.modrinth.install.InstallRequest

interface InstallerState {
    @Composable
    fun showHiddenDependencyDialog()

    fun buttonCompose(request: InstallRequest)

    fun buttonDecompose(request: InstallRequest)

    fun startInstall(request: InstallRequest)

    fun loading(request: InstallRequest): Boolean

    fun loadingMsg(request: InstallRequest): String

    @Composable
    fun showButtonDependencyPopup(request: InstallRequest)
}
