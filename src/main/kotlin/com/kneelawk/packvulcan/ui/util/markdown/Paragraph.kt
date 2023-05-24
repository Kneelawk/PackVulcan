package com.kneelawk.packvulcan.ui.util.markdown

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.vladsch.flexmark.ast.*
import java.awt.Cursor

class MDParagraph(
    private val children: List<MDNode>, private val alignment: FlowMainAxisAlignment = FlowMainAxisAlignment.Start,
    private val link: String? = null, private val underline: Boolean = false, private val padding: Dp = 0.dp
) : MDNode {
    companion object {
        fun parse(parent: Paragraph, ctx: MDContext): MDParagraph {
            var builder = AnnotatedString.Builder()
            var child = parent.firstChild
            val children = mutableListOf<MDNode>()

            println("Paragraph [")
            while (child != null) {
                println("  $child")

                when {
                    child is Link && (child.firstChild is Image || child.firstChild is ImageRef) -> {
                        val childChild = child.firstChild
                        if (childChild is Image) {
                            builder.dump(children, ctx)
                            children.add(MDImage.parse(childChild, child.url.unescape()))
                            builder = AnnotatedString.Builder()
                        }
                    }
                    child is Text || child is Emphasis || child is StrongEmphasis || child is Link || child is LinkRef -> builder.appendMarkdownChild(
                        child, ctx.pvColors
                    )
                    child is Image -> {
                        builder.dump(children, ctx)
                        children.add(MDImage.parse(child))
                        builder = AnnotatedString.Builder()
                    }
                    child is SoftLineBreak -> {
                        if (builder.length > 0) {
                            builder.append(" ")
                        }
                    }
                    child is HardLineBreak -> {
                        builder.dump(children, ctx)
                        children.add(MDHardLineBreak)
                        builder = AnnotatedString.Builder()
                    }
                    child is HtmlInline -> {
                        val mdChild = MDHTML.parseInline(child, ctx)
                        if (mdChild != null) {
                            builder.dump(children, ctx)
                            children.add(mdChild)
                            builder = AnnotatedString.Builder()
                        }
                    }
                }

                child = child.next
            }
            println("]")

            builder.dump(children, ctx)

            return MDParagraph(children)
        }

        private fun AnnotatedString.Builder.dump(children: MutableList<MDNode>, ctx: MDContext) {
            if (length > 0) {
                children.add(toMDText(ctx.typography.body1.copy(color = ctx.colors.onSurface)))
            }
        }
    }

    @Composable
    override fun render() {
        Column(modifier = Modifier.padding(bottom = padding)) {
            val uriHandler = LocalUriHandler.current
            var modifier: Modifier = Modifier.fillMaxWidth()

            if (link != null) {
                modifier = modifier.clickable {
                    uriHandler.openUri(link)
                }.pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
            }

            FlowRow(
                crossAxisAlignment = FlowCrossAxisAlignment.End, mainAxisAlignment = alignment, modifier = modifier
            ) {
                for ((index, child) in children.withIndex()) {
                    key(index) {
                        Box {
                            child.render()
                        }
                    }
                }
            }

            if (underline) {
                Divider()
            }
        }
    }

    override fun toString(): String {
        return "MDParagraph(children=${
            children.joinToString(
                ",\n", "[\n", "\n]"
            )
        }, alignment=$alignment, link=$link, underline=$underline)"
    }
}

class MDSpan(
    private val children: List<MDNode>, private val alignment: FlowMainAxisAlignment = FlowMainAxisAlignment.Start,
    private val link: String? = null
) : MDNode {
    @Composable
    override fun render() {
        val uriHandler = LocalUriHandler.current
        var modifier: Modifier = Modifier

        if (link != null) {
            modifier = modifier.clickable {
                uriHandler.openUri(link)
            }.pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
        }

        FlowRow(
            crossAxisAlignment = FlowCrossAxisAlignment.End, mainAxisAlignment = alignment, modifier = modifier
        ) {
            for ((index, child) in children.withIndex()) {
                key(index) {
                    child.render()
                }
            }
        }
    }

    override fun toString(): String {
        return "MDSpan(children=${children.joinToString(",\n", "[\n", "\n]")}, alignment=$alignment, link=$link)"
    }
}
