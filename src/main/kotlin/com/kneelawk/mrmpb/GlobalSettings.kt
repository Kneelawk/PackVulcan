package com.kneelawk.mrmpb

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.moandjiezana.toml.Toml
import com.moandjiezana.toml.TomlWriter
import mu.KotlinLogging
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

object GlobalSettings {
    private val log = KotlinLogging.logger {}

    private const val folderName = ".mrmpb"
    private val folder = Paths.get(System.getProperty("user.home"), folderName)
    private val settingsFile = folder.resolve("settings.toml")

    private val defaultDarkMode = true
    private lateinit var darkModeState: MutableState<Boolean>

    fun load() {
        log.info("Loading global settings...")

        if (!folder.exists()) {
            folder.createDirectories()
        }

        if (settingsFile.exists()) {
            val root = Toml().read(settingsFile.inputStream())

            val uiSettings = root.getTable("ui") ?: Toml()
            darkModeState = mutableStateOf(uiSettings.getBoolean("dark-mode", defaultDarkMode))
        } else {
            darkModeState = mutableStateOf(defaultDarkMode)
        }
    }

    fun store() {
        log.info("Storing settings...")

        if (!folder.exists()) {
            folder.createDirectories()
        }

        val writer = TomlWriter()

        val root = mapOf(
            "ui" to mapOf(
                "dark-mode" to darkModeState.value
            )
        )
        writer.write(root, settingsFile.outputStream())
    }

    var darkMode
        get() = darkModeState.value
        set(value) {
            darkModeState.value = value
        }
}