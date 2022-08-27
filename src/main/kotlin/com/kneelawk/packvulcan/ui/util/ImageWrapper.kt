package com.kneelawk.packvulcan.ui.util

import androidx.compose.foundation.Image
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import com.kneelawk.packvulcan.ui.util.widgets.ButtonIcon
import androidx.compose.ui.graphics.ImageBitmap as ComposeImageBitmap
import androidx.compose.ui.graphics.painter.Painter as ComposePainter
import androidx.compose.ui.graphics.vector.ImageVector as ComposeImageVector

sealed class ImageWrapper {
    @Composable
    protected abstract fun imageImpl(
        contentDescription: String?, modifier: Modifier, alignment: Alignment, contentScale: ContentScale,
        alpha: Float, colorFilter: ColorFilter?, filterQuality: FilterQuality
    )

    @Composable
    protected abstract fun iconImpl(contentDescription: String?, modifier: Modifier, tint: Color)

    @Composable
    protected abstract fun buttonIconImpl(contentDescription: String?, size: Dp, modifier: Modifier, tint: Color)

    @Composable
    fun image(
        contentDescription: String?, modifier: Modifier = Modifier, alignment: Alignment = Alignment.Center,
        contentScale: ContentScale = ContentScale.Fit, alpha: Float = DefaultAlpha, colorFilter: ColorFilter? = null,
        filterQuality: FilterQuality = DrawScope.DefaultFilterQuality
    ) {
        imageImpl(contentDescription, modifier, alignment, contentScale, alpha, colorFilter, filterQuality)
    }

    @Composable
    fun icon(
        contentDescription: String?, modifier: Modifier = Modifier,
        tint: Color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
    ) {
        iconImpl(contentDescription, modifier, tint)
    }

    @Composable
    fun buttonIcon(
        contentDescription: String?, size: Dp = ButtonDefaults.IconSize, modifier: Modifier = Modifier,
        tint: Color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
    ) {
        buttonIconImpl(contentDescription, size, modifier, tint)
    }

    data class Painter(val painter: ComposePainter) : ImageWrapper() {
        @Composable
        override fun imageImpl(
            contentDescription: String?, modifier: Modifier, alignment: Alignment, contentScale: ContentScale,
            alpha: Float, colorFilter: ColorFilter?, filterQuality: FilterQuality
        ) {
            Image(painter, contentDescription, modifier, alignment, contentScale, alpha, colorFilter)
        }

        @Composable
        override fun iconImpl(contentDescription: String?, modifier: Modifier, tint: Color) {
            Icon(painter, contentDescription, modifier, tint)
        }

        @Composable
        override fun buttonIconImpl(contentDescription: String?, size: Dp, modifier: Modifier, tint: Color) {
            ButtonIcon(painter, contentDescription, size, modifier, tint)
        }
    }

    data class ImageBitmap(val bitmap: ComposeImageBitmap) : ImageWrapper() {
        @Composable
        override fun imageImpl(
            contentDescription: String?, modifier: Modifier, alignment: Alignment, contentScale: ContentScale,
            alpha: Float, colorFilter: ColorFilter?, filterQuality: FilterQuality
        ) {
            Image(bitmap, contentDescription, modifier, alignment, contentScale, alpha, colorFilter, filterQuality)
        }

        @Composable
        override fun iconImpl(contentDescription: String?, modifier: Modifier, tint: Color) {
            Icon(bitmap, contentDescription, modifier, tint)
        }

        @Composable
        override fun buttonIconImpl(contentDescription: String?, size: Dp, modifier: Modifier, tint: Color) {
            ButtonIcon(bitmap, contentDescription, size, modifier, tint)
        }
    }

    data class ImageVector(val vector: ComposeImageVector) : ImageWrapper() {
        @Composable
        override fun imageImpl(
            contentDescription: String?, modifier: Modifier, alignment: Alignment, contentScale: ContentScale,
            alpha: Float, colorFilter: ColorFilter?, filterQuality: FilterQuality
        ) {
            Image(vector, contentDescription, modifier, alignment, contentScale, alpha, colorFilter)
        }

        @Composable
        override fun iconImpl(contentDescription: String?, modifier: Modifier, tint: Color) {
            Icon(vector, contentDescription, modifier, tint)
        }

        @Composable
        override fun buttonIconImpl(contentDescription: String?, size: Dp, modifier: Modifier, tint: Color) {
            ButtonIcon(vector, contentDescription, size, modifier, tint)
        }
    }
}
