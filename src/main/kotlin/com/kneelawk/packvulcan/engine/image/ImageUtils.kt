package com.kneelawk.packvulcan.engine.image

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import kotlin.math.min
import kotlin.math.roundToInt

object ImageUtils {
    const val MOD_ICON_SIZE = 96

    suspend fun scaleImage(input: BufferedImage, maxSideLength: Int): BufferedImage = withContext(Dispatchers.IO) {
        val maxSideLengthF = maxSideLength.toFloat()
        val widthF = input.width.toFloat()
        val heightF = input.height.toFloat()
        val ratio = min(maxSideLengthF / widthF, maxSideLengthF / heightF)
        val width = (ratio * widthF).roundToInt()
        val height = (ratio * heightF).roundToInt()

        val new = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = new.createGraphics()
        g.setRenderingHints(
            mapOf(
                RenderingHints.KEY_RENDERING to RenderingHints.VALUE_RENDER_QUALITY,
                RenderingHints.KEY_ANTIALIASING to RenderingHints.VALUE_ANTIALIAS_ON,
                RenderingHints.KEY_INTERPOLATION to if (ratio > 1f) RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR else RenderingHints.VALUE_INTERPOLATION_BICUBIC
            )
        )
        g.drawImage(input, 0, 0, width, height, null)
        g.dispose()

        new
    }
}