package com.kneelawk.mrmpb.ui.theme

import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.unit.Density

object MrMpBIcons {
    val file = loadSvgPainter(
        MrMpBIcons.javaClass.classLoader.getResourceAsStream("description_black_24dp.svg")
            ?: throw IllegalStateException("Unable to load file svg"), Density(1f)
    )
    val folder = loadSvgPainter(
        MrMpBIcons.javaClass.classLoader.getResourceAsStream("folder_black_24dp.svg")
            ?: throw IllegalStateException("Unable to load folder svg"), Density(1f)
    )
    val create_new_folder = loadSvgPainter(
        MrMpBIcons.javaClass.classLoader.getResourceAsStream("create_new_folder_black_24dp.svg")
            ?: throw IllegalStateException("Unable to load create-new-folder svg"), Density(1f)
    )
}