package com.kneelawk.packvulcan.ui.util

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.unit.Density

fun loadSvgPainter(
    svgString: String,
    density: Density
): Painter {
    val inputStream = svgString.byteInputStream()
    return loadSvgPainter(inputStream, density)
}
