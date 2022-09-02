package com.kneelawk.packvulcan.ui.modrinth.install

interface InstallInterface {
    val loading: Boolean
    val loadingText: String
    val modName: String
    val modVersion: String
    val collectedDependencies: List<DependencyDisplay>

    fun cancel()

    fun install()
}
