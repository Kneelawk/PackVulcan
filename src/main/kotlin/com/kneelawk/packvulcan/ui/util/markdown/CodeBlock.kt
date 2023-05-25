package com.kneelawk.packvulcan.ui.util.markdown

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.vladsch.flexmark.util.ast.ContentNode

class MDCodeBlock(private val code: String) : MDNode {
    companion object {
        fun parse(block: ContentNode): MDCodeBlock {
            return MDCodeBlock(block.contentChars.unescape().trim().replace("\t", "    "))
        }
    }

    @Composable
    override fun render() {
        Box(Modifier.background(MaterialTheme.colors.secondary, RoundedCornerShape(5.dp)).fillMaxWidth()) {
            Text(
                text = code,
                modifier = Modifier.padding(5.dp),
                fontFamily = FontFamily.Monospace
            )
        }
    }

    override fun toString(): String {
        return "MDCodeBlock(code='$code')"
    }
}
