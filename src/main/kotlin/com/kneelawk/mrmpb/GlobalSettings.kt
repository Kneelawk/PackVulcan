package com.kneelawk.mrmpb

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.moandjiezana.toml.Toml
import com.moandjiezana.toml.TomlWriter
import mu.KotlinLogging
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.io.path.pathString

object GlobalSettings {
    private val log = KotlinLogging.logger {}

    private val defaultDarkMode = true
    private lateinit var darkModeState: MutableState<Boolean>

    lateinit var fileChooserFavoritesList: SnapshotStateList<Path>
        private set

    fun load() {
        log.info("Loading global settings...")

        if (GlobalConstants.SETTINGS_FILE.exists()) {
            val root = Toml().read(GlobalConstants.SETTINGS_FILE.inputStream())

            val uiSettings = root.getTable("ui") ?: Toml()
            darkModeState = mutableStateOf(uiSettings.getBoolean("dark-mode", defaultDarkMode))
            fileChooserFavoritesList =
                mutableStateListOf(
                    *(uiSettings.getList<String>("file-chooser-favorites")?.map { Paths.get(it) }?.toTypedArray()
                        ?: arrayOf())
                )
        } else {
            darkModeState = mutableStateOf(defaultDarkMode)
            fileChooserFavoritesList = mutableStateListOf()
        }
    }

    fun store() {
        log.info("Storing settings...")

        val writer = TomlWriter()

        val root = mapOf(
            "ui" to mapOf(
                "dark-mode" to darkModeState.value,
                "file-chooser-favorites" to fileChooserFavoritesList.map { it.pathString }
            )
        )
        writer.write(root, GlobalConstants.SETTINGS_FILE.outputStream())
    }

    var darkMode
        get() = darkModeState.value
        set(value) {
            darkModeState.value = value
        }
}