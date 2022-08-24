package com.kneelawk.packvulcan.model

import java.awt.image.BufferedImage

sealed class ModIconSource {
    data class Url(val url: String) : ModIconSource()
    data class Buffered(val image: BufferedImage) : ModIconSource()
}
