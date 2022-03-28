package com.kneelawk.mrmpb

import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

object GlobalConstants {
    private const val FOLDER_NAME = ".mrmpb"
    private val FOLDER = Paths.get(System.getProperty("user.home"), FOLDER_NAME)
    val SETTINGS_FILE: Path = FOLDER.resolve("settings.toml")

    const val INITIAL_PROJECT_VERSION = "0.1.0"
    const val INITIAL_MINECRAFT_VERSION = "1.18.2"
    const val INITIAL_LOADER_VERSION = "Fabric 0.13.3"

    init {
        if (!FOLDER.exists()) {
            FOLDER.createDirectories()
        }
    }
}