package com.kneelawk.mrmpb.ui.theme

import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.unit.Density
import java.io.InputStream

object MrMpBIcons {
    private val loader = MrMpBIcons.javaClass.classLoader

    private fun InputStream?.orError(resourceName: String): InputStream {
        if (this == null) {
            throw IllegalStateException("Unable to load $resourceName icon")
        } else {
            return this
        }
    }

    val create_new_folder = loader.getResourceAsStream("create_new_folder_black_24dp.svg").orError("create-new-folder")
        .use { loadSvgPainter(it, Density(1f)) }
    val desktop = loader.getResourceAsStream("desktop_mac_black_24dp.svg").orError("desktop")
        .use { loadSvgPainter(it, Density(1f)) }
    val download = loader.getResourceAsStream("download_black_24dp.svg").orError("download")
        .use { loadSvgPainter(it, Density(1f)) }
    val file = loader.getResourceAsStream("description_black_24dp.svg").orError("file")
        .use { loadSvgPainter(it, Density(1f)) }
    val folder = loader.getResourceAsStream("folder_black_24dp.svg").orError("folder")
        .use { loadSvgPainter(it, Density(1f)) }
    val image = loader.getResourceAsStream("image_black_24dp.svg").orError("image")
        .use { loadSvgPainter(it, Density(1f)) }
    val movie = loader.getResourceAsStream("movie_black_24dp.svg").orError("movie")
        .use { loadSvgPainter(it, Density(1f)) }
    val music = loader.getResourceAsStream("music_note_black_24dp.svg").orError("music")
        .use { loadSvgPainter(it, Density(1f)) }
}