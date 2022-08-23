package com.kneelawk.packvulcan.ui.theme

import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.unit.Density
import java.io.InputStream

object PackVulcanIcons {
    private val loader = PackVulcanIcons.javaClass.classLoader

    private fun InputStream?.orError(resourceName: String): InputStream {
        if (this == null) {
            throw IllegalStateException("Unable to load $resourceName icon")
        } else {
            return this
        }
    }

    val bolt = loader.getResourceAsStream("bolt_black_24dp.svg").orError("bolt")
        .use { loadSvgPainter(it, Density(1f)) }
    val bug = loader.getResourceAsStream("bug_report_black_24dp.svg").orError("bug")
        .use { loadSvgPainter(it, Density(1f)) }
    val codeBox = loader.getResourceAsStream("integration_instructions_black_24dp.svg").orError("codeBox")
        .use { loadSvgPainter(it, Density(1f)) }
    val createNewFolder = loader.getResourceAsStream("create_new_folder_black_24dp.svg").orError("createNewFolder")
        .use { loadSvgPainter(it, Density(1f)) }
    val desktop = loader.getResourceAsStream("desktop_mac_black_24dp.svg").orError("desktop")
        .use { loadSvgPainter(it, Density(1f)) }
    val download = loader.getResourceAsStream("download_black_24dp.svg").orError("download")
        .use { loadSvgPainter(it, Density(1f)) }
    val compass = loader.getResourceAsStream("explore_black_24dp.svg").orError("compass")
        .use { loadSvgPainter(it, Density(1f)) }
    val error = loader.getResourceAsStream("error_black_24dp.svg").orError("error")
        .use { loadSvgPainter(it, Density(1f)) }
    val fancySuitcase = loader.getResourceAsStream("business_center_black_24dp.svg").orError("fancySuitcase")
        .use { loadSvgPainter(it, Density(1f)) }
    val file = loader.getResourceAsStream("description_black_24dp.svg").orError("file")
        .use { loadSvgPainter(it, Density(1f)) }
    val flame = loader.getResourceAsStream("local_fire_department_black_24dp.svg").orError("flame")
        .use { loadSvgPainter(it, Density(1f)) }
    val folder = loader.getResourceAsStream("folder_black_24dp.svg").orError("folder")
        .use { loadSvgPainter(it, Density(1f)) }
    val house = loader.getResourceAsStream("house_black_24dp.svg").orError("house")
        .use { loadSvgPainter(it, Density(1f)) }
    val image = loader.getResourceAsStream("image_black_24dp.svg").orError("image")
        .use { loadSvgPainter(it, Density(1f)) }
    val inventory = loader.getResourceAsStream("inventory_black_24dp.svg").orError("inventory")
        .use { loadSvgPainter(it, Density(1f)) }
    val laptop = loader.getResourceAsStream("laptop_black_24dp.svg").orError("laptop")
        .use { loadSvgPainter(it, Density(1f)) }
    val microscope = loader.getResourceAsStream("biotech_black_24dp.svg").orError("microscope")
        .use { loadSvgPainter(it, Density(1f)) }
    val modrinth = loader.getResourceAsStream("modrinth_icon_24dp.svg").orError("modrinth")
        .use { loadSvgPainter(it, Density(1f)) }
    val movie = loader.getResourceAsStream("movie_black_24dp.svg").orError("movie")
        .use { loadSvgPainter(it, Density(1f)) }
    val music = loader.getResourceAsStream("music_note_black_24dp.svg").orError("music")
        .use { loadSvgPainter(it, Density(1f)) }
    val noImage = loader.getResourceAsStream("no_photography_black_24dp.svg").orError("noImage")
        .use { loadSvgPainter(it, Density(1f)) }
    val public = loader.getResourceAsStream("public_black_24dp.svg").orError("public")
        .use { loadSvgPainter(it, Density(1f)) }
    val restaurant = loader.getResourceAsStream("restaurant_black_24dp.svg").orError("restaurant")
        .use { loadSvgPainter(it, Density(1f)) }
    val save = loader.getResourceAsStream("save_black_24dp.svg").orError("save")
        .use { loadSvgPainter(it, Density(1f)) }
    val shield = loader.getResourceAsStream("shield_black_24dp.svg").orError("shield")
        .use { loadSvgPainter(it, Density(1f)) }
    val storage = loader.getResourceAsStream("storage_black_24dp.svg").orError("storage")
        .use { loadSvgPainter(it, Density(1f)) }
    val fabric = loader.getResourceAsStream("fabric_smooth_filled_24dp.svg").orError("fabric")
        .use { loadSvgPainter(it, Density(1f)) }
    val forge = loader.getResourceAsStream("conda_forge_filled_24dp.svg").orError("forge")
        .use { loadSvgPainter(it, Density(1f)) }
    val quilt = loader.getResourceAsStream("quilt_logo_mono_black_transparent_24dp.svg").orError("quilt")
        .use { loadSvgPainter(it, Density(1f)) }
}
