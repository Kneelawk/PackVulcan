package com.kneelawk.packvulcan.ui.modrinth.install

sealed interface InstallVersion {
    object Latest : InstallVersion
    data class Specific(val versionId: String) : InstallVersion
}
