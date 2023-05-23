package com.kneelawk.packvulcan.ui.util.markdown

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kneelawk.packvulcan.model.IconSource
import com.kneelawk.packvulcan.ui.util.widgets.AsyncIcon
import com.vladsch.flexmark.ast.Image

class MDImage(private val src: IconSource) : MDNode {
    companion object {
        fun parse(image: Image): MDImage {
            return MDImage(IconSource.Url(image.url.unescape()))
        }
    }

    @Composable
    override fun render() {
        AsyncIcon(src, Modifier.width(IntrinsicSize.Max))
    }

    override fun toString(): String {
        return "MDImage(src=$src)"
    }
}
