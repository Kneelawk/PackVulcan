package com.kneelawk.packvulcan

import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

object GlobalConstants {
    const val VERSION = "0.1.0"
    const val REST_USER_AGENT = "PackVulcan/$VERSION"

    private const val FOLDER_NAME = ".packvulcan"
    private val FOLDER = Paths.get(System.getProperty("user.home"), FOLDER_NAME)
    val SETTINGS_FILE: Path = FOLDER.resolve("settings.toml")
    private val CACHE_DIR_PATH: Path = FOLDER.resolve("cache")
    val MOD_CACHE_DIR_PATH: Path = CACHE_DIR_PATH.resolve("mods")

    val HOME_FOLDER: Path = Paths.get(System.getProperty("user.home"))

    const val INITIAL_PROJECT_VERSION = "0.1.0"
    const val INITIAL_MINECRAFT_VERSION = "1.19.2"
    const val INITIAL_LOADER_VERSION = "Quilt 0.17.4"
    const val MAX_DOWNLOAD_ATTEMPTS = 10

    init {
        if (!FOLDER.exists()) {
            FOLDER.createDirectories()
        }
    }
}
