package com.kneelawk.packvulcan.ui.util.markdown

import com.kneelawk.packvulcan.model.IconSource
import com.vladsch.flexmark.ast.HtmlBlock
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.safety.Cleaner
import org.jsoup.safety.Safelist

object MDHTML {
    fun parseBlock(block: HtmlBlock): MDNode? {
        val str = block.chars.unescape()

        val cleaner = Cleaner(Safelist.basicWithImages())
        val doc = cleaner.clean(Jsoup.parseBodyFragment(str))
        val body = doc.body()
        val tag = body.child(0)

        when (tag.tag().name) {
            "img" -> {
                return parseImg(tag)
            }
        }

        return null
    }

    private fun parseImg(tag: Element): MDNode? {
        val src = tag.attr("src")
        if (src.isNotEmpty()) {
            val widthAttr = tag.attr("width")
            val width = when {
                widthAttr.endsWith("%") -> {
                    widthAttr.substring(0, widthAttr.length - 1).toFloatOrNull()?.let { MDImage.Percent(it / 100f) }
                        ?: MDImage.Max
                }
                widthAttr.isNotBlank() -> {
                    widthAttr.toFloatOrNull()?.let { MDImage.Fixed(it) } ?: MDImage.Max
                }
                else -> MDImage.Max
            }

            return MDImage(IconSource.Url(src), width = width)
        }

        return null
    }
}
