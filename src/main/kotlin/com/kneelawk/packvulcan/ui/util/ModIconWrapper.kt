package com.kneelawk.packvulcan.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector

data class ModIconWrapper(val image: ImageWrapper, val type: Type) {
    companion object {
        fun image(image: ImageBitmap) = ModIconWrapper(ImageWrapper.ImageBitmap(image), Type.IMAGE)
        fun image(painter: Painter) = ModIconWrapper(ImageWrapper.Painter(painter), Type.IMAGE)
        fun image(vector: ImageVector) = ModIconWrapper(ImageWrapper.ImageVector(vector), Type.IMAGE)
        fun icon(painter: Painter) = ModIconWrapper(ImageWrapper.Painter(painter), Type.ICON)
        fun icon(vector: ImageVector) = ModIconWrapper(ImageWrapper.ImageVector(vector), Type.IMAGE)

        fun image(image: ImageWrapper) = ModIconWrapper(image, Type.IMAGE)
        fun icon(icon: ImageWrapper) = ModIconWrapper(icon, Type.ICON)
    }

    @Composable
    fun draw(contentDescription: String?, modifier: Modifier = Modifier) {
        when (type) {
            Type.IMAGE -> image.image(contentDescription, modifier)
            Type.ICON -> image.icon(contentDescription, modifier)
        }
    }

    enum class Type {
        IMAGE, ICON;
    }
}
