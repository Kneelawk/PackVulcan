package com.kneelawk.packvulcan.ui.util.markdown

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Colors
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.text.AnnotatedString
import com.vladsch.flexmark.ast.*

class MDParagraph(private val children: List<MDNode>) : MDNode {
    companion object {
        fun parse(parent: Paragraph, typography: Typography, colors: Colors): MDParagraph {
            var builder = AnnotatedString.Builder()
            var child = parent.firstChild
            val children = mutableListOf<MDNode>()

            while (child != null) {
                when (child) {
                    is Text, is Emphasis, is StrongEmphasis -> builder.appendMarkdownChild(child, colors)
                    is Image -> {
                        children.add(builder.toMDText(typography.body1.copy(color = colors.onSurface)))
                        children.add(MDImage.parse(child))
                        builder = AnnotatedString.Builder()
                    }
                }

                child = child.next
            }

            if (builder.length > 0) {
                children.add(builder.toMDText(typography.body1.copy(color = colors.onSurface)))
            }

            return MDParagraph(children)
        }
    }

    @Composable
    override fun render() {
        Column {
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
