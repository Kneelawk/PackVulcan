package com.kneelawk.packvulcan.engine.modrinth.install

sealed interface InstallVersion {
    object Latest : InstallVersion
    data class Specific(val versionId: String) : InstallVersion
}
