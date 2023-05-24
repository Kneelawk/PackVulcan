package com.kneelawk.packvulcan.ui.util.markdown

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.kneelawk.packvulcan.model.IconSource
import com.kneelawk.packvulcan.ui.util.widgets.AsyncIcon
import com.vladsch.flexmark.ast.Image

class MDImage(private val src: IconSource, private val link: String? = null, private val width: Width = Max) : MDNode {
    companion object {
        fun parse(image: Image, link: String? = null): MDImage {
            return MDImage(IconSource.Url(image.url.unescape()), link)
        }
    }

    @Composable
    override fun render() {
        val uriHandler = LocalUriHandler.current
        var modifier: Modifier = Modifier

        modifier = with(width) { modifier.applyWidth() }

        if (link != null) {
            modifier = modifier.clickable {
                uriHandler.openUri(link)
            }
        }

        AsyncIcon(src, modifier)
    }

    override fun toString(): String {
        return "MDImage(src=$src, link=$link, width=$width)"
    }

    sealed interface Width {
        fun Modifier.applyWidth(): Modifier
    }

    object Max : Width {
        override fun Modifier.applyWidth(): Modifier = width(IntrinsicSize.Max)
    }

    data class Fixed(val width: Float) : Width {
        override fun Modifier.applyWidth(): Modifier = width(width.dp)
    }

    data class Percent(val frac: Float) : Width {
        override fun Modifier.applyWidth(): Modifier = fillMaxWidth(frac)
    }
}
