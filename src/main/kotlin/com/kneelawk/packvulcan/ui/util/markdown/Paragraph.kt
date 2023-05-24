package com.kneelawk.packvulcan.ui.util.markdown

import androidx.compose.material.Colors
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.kneelawk.packvulcan.ui.theme.PackVulcanColors
import com.vladsch.flexmark.ast.*

class MDParagraph(private val children: List<MDNode>) : MDNode {
    companion object {
        fun parse(parent: Paragraph, typography: Typography, colors: Colors, pvColors: PackVulcanColors): MDParagraph {
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
                            builder.dump(children, typography, colors)
                            children.add(MDImage.parse(childChild, child.url.unescape()))
                            builder = AnnotatedString.Builder()
                        }
                    }
                    child is Text || child is Emphasis || child is StrongEmphasis || child is Link -> builder.appendMarkdownChild(
                        child, pvColors
                    )
                    child is Image -> {
                        builder.dump(children, typography, colors)
                        children.add(MDImage.parse(child))
                        builder = AnnotatedString.Builder()
                    }
                    child is SoftLineBreak -> {
                        if (builder.length > 0) {
                            builder.append(" ")
                        }
                    }
                }

                child = child.next
            }
            println("]")

            builder.dump(children, typography, colors)

            return MDParagraph(children)
        }

        private fun AnnotatedString.Builder.dump(
            children: MutableList<MDNode>, typography: Typography, colors: Colors
        ) {
            if (length > 0) {
                children.add(toMDText(typography.body1.copy(color = colors.onSurface)))
            }
        }
    }

    @Composable
    override fun render() {
        FlowRow(mainAxisSpacing = 2.dp) {
            for ((index, child) in children.withIndex()) {
                key(index) {
                    child.render()
                }
            }
        }
    }

    override fun toString(): String {
        return "MDParagraph(children=${children.joinToString(",\n", "[\n", "\n]")})"
    }
}
