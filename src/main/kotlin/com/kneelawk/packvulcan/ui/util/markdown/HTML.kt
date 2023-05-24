package com.kneelawk.packvulcan.ui.util.markdown

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.kneelawk.packvulcan.model.IconSource
import com.vladsch.flexmark.ast.HtmlBlock
import com.vladsch.flexmark.ast.HtmlInline
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.safety.Cleaner
import org.jsoup.safety.Safelist

object MDHTML {
    fun parseBlock(
        block: HtmlBlock, ctx: MDContext
    ): MDNode? {
        val str = block.chars.unescape()

        val cleaner = makeCleaner()
        val doc = cleaner.clean(Jsoup.parseBodyFragment(str))
        val body = doc.body()

        if (body.childrenSize() > 0) {
            val tag = body.child(0)
            return parseBlock(tag, ctx)
        }

        return null
    }

    fun parseBlock(
        tag: Element, ctx: MDContext, alignment: FlowMainAxisAlignment = FlowMainAxisAlignment.Start
    ): MDNode? {
        when (tag.tag().name) {
            "img" -> return parseImg(tag)
            "center" -> return HTMLCenter.parse(tag, ctx)
            "p" -> return parseParagraph(tag, ctx, ctx.typography.body1.copy(color = ctx.colors.onSurface), alignment)
            "h1", "h2", "h3", "h4", "h5", "h6" -> return parseHeading(tag, ctx, alignment)
        }

        return null
    }

    fun parseInline(inline: HtmlInline, ctx: MDContext): MDNode? {
        val str = inline.chars.unescape()

        val cleaner = makeCleaner()
        val doc = cleaner.clean(Jsoup.parseBodyFragment(str))
        val body = doc.body()

        if (body.childrenSize() > 0) {
            val tag = body.child(0)
            return parseInline(tag, ctx, ctx.typography.body1.copy(color = ctx.colors.onSurface))
        }

        return null
    }

    private fun makeCleaner() = Cleaner(
        Safelist.basicWithImages().addTags("center", "h1", "h2", "h3", "h4", "h5", "h6").addAttributes("p", "align")
    )

    fun parseInline(
        tag: Element, ctx: MDContext, style: TextStyle, alignment: FlowMainAxisAlignment = FlowMainAxisAlignment.Start
    ): MDNode? {
        println("Parsing inline: $tag")
        when (tag.tag().name) {
            "br" -> return MDHardLineBreak
            "a" -> return parseLink(tag, ctx, style)
            "img" -> return parseImg(tag)
            "h1", "h2", "h3", "h4", "h5", "h6" -> return parseHeading(tag, ctx, alignment)
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

    private fun parseHeading(tag: Element, ctx: MDContext, alignment: FlowMainAxisAlignment): MDNode? {
        val (style, underline) = when (tag.tag().name) {
            "h1" -> (ctx.pvTypography.mdH1.copy(color = ctx.pvColors.headingColor) to true)
            "h2" -> (ctx.pvTypography.mdH2.copy(color = ctx.pvColors.headingColor) to true)
            "h3" -> (ctx.pvTypography.mdH3.copy(color = ctx.pvColors.headingColor) to false)
            "h4" -> (ctx.pvTypography.mdH4.copy(color = ctx.colors.onSurface) to false)
            "h5" -> (ctx.pvTypography.mdH5.copy(color = ctx.colors.onSurface) to false)
            "h6" -> (ctx.pvTypography.mdH6.copy(color = ctx.colors.onSurface) to false)
            else -> {
                return null
            }
        }

        return parseParagraph(
            tag.childNodes(), ctx, style, alignment = alignment, underline = underline, padding = 8.dp
        )
    }

    private fun parseParagraph(
        tag: Element, ctx: MDContext, style: TextStyle, alignment: FlowMainAxisAlignment
    ): MDNode {
        println("Parsing paragraph: $tag")
        var align = alignment
        val alignAttr = tag.attr("align")
        if (alignAttr.isNotEmpty()) {
            align = when (alignAttr) {
                "center" -> FlowMainAxisAlignment.Center
                "right" -> FlowMainAxisAlignment.End
                else -> FlowMainAxisAlignment.Start
            }
        }

        return parseParagraph(tag.childNodes(), ctx, style, alignment = align)
    }

    private fun parseLink(tag: Element, ctx: MDContext, style: TextStyle): MDNode? {
        val href = tag.attr("href")
        if (href.isNotEmpty()) {
            return parseSpan(
                tag.childNodes(), ctx,
                style.copy(color = ctx.pvColors.linkColor, textDecoration = TextDecoration.Underline), link = href
            )
        }

        return null
    }

    private fun parseParagraph(
        childNodes: List<Node>, ctx: MDContext, style: TextStyle,
        alignment: FlowMainAxisAlignment = FlowMainAxisAlignment.Start, link: String? = null,
        underline: Boolean = false, padding: Dp = 0.dp
    ): MDParagraph {
        val children = extractChildren(childNodes, ctx, style, alignment)

        return MDParagraph(children, alignment = alignment, link = link, underline = underline, padding = padding)
    }

    private fun parseSpan(
        childNodes: List<Node>, ctx: MDContext, style: TextStyle,
        alignment: FlowMainAxisAlignment = FlowMainAxisAlignment.Start, link: String? = null
    ): MDSpan {
        val children = extractChildren(childNodes, ctx, style, alignment)

        return MDSpan(children, alignment = alignment, link = link)
    }

    private fun extractChildren(
        childNodes: List<Node>, ctx: MDContext, style: TextStyle, alignment: FlowMainAxisAlignment
    ): MutableList<MDNode> {
        var builder = AnnotatedString.Builder()
        val children = mutableListOf<MDNode>()

        fun dump() {
            if (builder.length > 0) {
                children.add(builder.toMDText(style))
            }
        }

        for (child in childNodes) {
            if (child is TextNode) {
                builder.append(child.text())
            } else if (child is Element) {
                dump()
                parseInline(child, ctx, style, alignment)?.let(children::add)
                builder = AnnotatedString.Builder()
            }
        }

        dump()
        return children
    }
}

class HTMLCenter(private val children: List<MDNode>) : MDNode {
    companion object {
        fun parse(center: Element, ctx: MDContext): HTMLCenter {
            println("Parsing center: $center")

            val children = mutableListOf<MDNode>()

            for (child in center.children()) {
                MDHTML.parseBlock(child, ctx, FlowMainAxisAlignment.Center)?.let(children::add)
            }

            return HTMLCenter(children)
        }
    }

    @Composable
    override fun render() {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            for ((index, child) in children.withIndex()) {
                key(index) {
                    child.render()
                }
            }
        }
    }

    override fun toString(): String {
        return "HTMLCenter(children=${children.joinToString(",\n", "[\n", "\n]")})"
    }
}
