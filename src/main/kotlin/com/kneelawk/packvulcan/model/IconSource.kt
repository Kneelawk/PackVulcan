package com.kneelawk.packvulcan.model

import java.awt.image.BufferedImage

sealed class IconSource {
    data class Url(val url: String) : IconSource()
    data class Buffered(val image: BufferedImage) : IconSource()
}
