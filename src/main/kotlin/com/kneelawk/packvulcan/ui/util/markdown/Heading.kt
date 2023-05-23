package com.kneelawk.packvulcan.ui.util.markdown

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Colors
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vladsch.flexmark.ast.Heading
import com.vladsch.flexmark.util.ast.Document

class MDHeading(private val text: AnnotatedString, private val style: TextStyle, private val padding: Dp) : MDNode {
    companion object {
        fun parse(heading: Heading, typography: Typography, colors: Colors): MDHeading? {
            val style = when (heading.level) {
                1 -> typography.h1
                2 -> typography.h2
                3 -> typography.h3
                4 -> typography.h4
                5 -> typography.h5
                6 -> typography.h6
                else -> {
                    return null
                }
            }.copy(color = colors.onSurface)

            val padding = if (heading.parent is Document) 8.dp else 0.dp

            val text = buildAnnotatedString {
                appendMarkdownChildren(heading, colors)
            }

            return MDHeading(text, style, padding)
        }
    }

    @Composable
    override fun render() {
        Box(modifier = Modifier.padding(bottom = padding)) {
            MarkdownText(text, style = style)
        }
    }

    override fun toString(): String {
        return "MDHeading(text=$text)"
    }
}
