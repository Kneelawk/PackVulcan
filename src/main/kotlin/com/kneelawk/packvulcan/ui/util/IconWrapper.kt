package com.kneelawk.packvulcan.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import com.kneelawk.packvulcan.model.IconSource
import com.kneelawk.packvulcan.net.image.ImageResource
import com.kneelawk.packvulcan.ui.theme.PackVulcanIcons

data class IconWrapper(val image: ImageWrapper, val type: Type) {
    companion object {
        suspend fun fromIconSource(icon: IconSource?): IconWrapper {
            return when (icon) {
                is IconSource.Buffered -> image(icon.image.toComposeImageBitmap())
                is IconSource.Url -> ImageResource.getModIcon(icon.url)?.let { image(it) }
                    ?: icon(ImageWrapper.Painter(PackVulcanIcons.noImage))

                null -> icon(ImageWrapper.Painter(PackVulcanIcons.noImage))
            }
        }

        fun image(image: ImageBitmap) = IconWrapper(ImageWrapper.ImageBitmap(image), Type.IMAGE)
        fun image(painter: Painter) = IconWrapper(ImageWrapper.Painter(painter), Type.IMAGE)
        fun image(vector: ImageVector) = IconWrapper(ImageWrapper.ImageVector(vector), Type.IMAGE)
        fun icon(painter: Painter) = IconWrapper(ImageWrapper.Painter(painter), Type.ICON)
        fun icon(vector: ImageVector) = IconWrapper(ImageWrapper.ImageVector(vector), Type.IMAGE)

        fun image(image: ImageWrapper) = IconWrapper(image, Type.IMAGE)
        fun icon(icon: ImageWrapper) = IconWrapper(icon, Type.ICON)
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