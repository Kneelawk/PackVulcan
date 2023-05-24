package com.kneelawk.packvulcan.ui.util.markdown

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Colors
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kneelawk.packvulcan.ui.theme.PackVulcanColors
import com.kneelawk.packvulcan.ui.theme.PackVulcanTypography
import com.vladsch.flexmark.ast.Heading
import com.vladsch.flexmark.util.ast.Document

class MDHeading(
    private val text: AnnotatedString, private val style: TextStyle, private val padding: Dp,
    private val underline: Boolean
) : MDNode {
    companion object {
        fun parse(
            heading: Heading, colors: Colors, pvTypography: PackVulcanTypography, pvColors: PackVulcanColors
        ): MDHeading? {
            val style = when (heading.level) {
                1 -> pvTypography.mdH1.copy(color = pvColors.headingColor)
                2 -> pvTypography.mdH2.copy(color = pvColors.headingColor)
                3 -> pvTypography.mdH3.copy(color = pvColors.headingColor)
                4 -> pvTypography.mdH4.copy(color = colors.onSurface)
                5 -> pvTypography.mdH5.copy(color = colors.onSurface)
                6 -> pvTypography.mdH6.copy(color = colors.onSurface)
                else -> {
                    return null
                }
            }

            val padding = if (heading.parent is Document) 8.dp else 0.dp

            val text = buildAnnotatedString {
                appendMarkdownChildren(heading, pvColors)
            }

            return MDHeading(text, style, padding, heading.level <= 2)
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
