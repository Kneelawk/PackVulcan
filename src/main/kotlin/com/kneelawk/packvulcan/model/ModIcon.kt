package com.kneelawk.packvulcan.model

import java.awt.image.BufferedImage

sealed class ModIcon {
    data class Url(val url: String) : ModIcon()
    data class Buffered(val image: BufferedImage) : ModIcon()
}
