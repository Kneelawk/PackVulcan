package com.kneelawk.packvulcan.model

enum class ModProvider(val prettyName: String) {
    MODRINTH("Modrinth"),
    CURSEFORGE("CurseForge"),
    GITHUB("GitHub"),
    URL("URL"),
    FILESYSTEM("Filesystem");
}