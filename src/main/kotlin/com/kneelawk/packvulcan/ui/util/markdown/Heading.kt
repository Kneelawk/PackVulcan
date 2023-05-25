package com.kneelawk.packvulcan.ui.util.markdown

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vladsch.flexmark.ast.Heading

class MDHeading(
    private val text: AnnotatedString, private val style: TextStyle, private val padding: Dp,
    private val underline: Boolean
) : MDNode {
    companion object {
        fun parse(
            heading: Heading, ctx: MDContext
        ): MDHeading? {
            val style = when (heading.level) {
                1 -> ctx.pvTypography.mdH1.copy(color = ctx.pvColors.headingColor)
                2 -> ctx.pvTypography.mdH2.copy(color = ctx.pvColors.headingColor)
                3 -> ctx.pvTypography.mdH3.copy(color = ctx.pvColors.headingColor)
                4 -> ctx.pvTypography.mdH4.copy(color = ctx.colors.onSurface)
                5 -> ctx.pvTypography.mdH5.copy(color = ctx.colors.onSurface)
                6 -> ctx.pvTypography.mdH6.copy(color = ctx.colors.onSurface)
                else -> {
                    return null
                }
            }

            val text = buildAnnotatedString {
                appendMarkdownChildren(heading, ctx)
            }

            return MDHeading(text, style, 8.dp, heading.level <= 2)
        }
    }

    @Composable
    override fun render() {
        Column(modifier = Modifier.padding(bottom = padding)) {
            MarkdownText(text, style = style)

            if (underline) {
                Divider()
            }
        }
    }

    override fun toString(): String {
        return "MDHeading(text=$text)"
    }
}
