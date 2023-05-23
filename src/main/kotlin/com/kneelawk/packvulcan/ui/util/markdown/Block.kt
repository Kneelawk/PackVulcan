package com.kneelawk.packvulcan.ui.util.markdown

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Colors
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.unit.dp
import com.vladsch.flexmark.ast.Heading
import com.vladsch.flexmark.ast.Paragraph
import com.vladsch.flexmark.util.ast.Node

class MDBlock(private val children: List<MDNode>) : MDNode {
    companion object {
        fun parse(node: Node, typography: Typography, colors: Colors): MDBlock {
            var child = node.firstChild
            val children = mutableListOf<MDNode>()

            while (child != null) {
                when (child) {
                    is Heading -> MDHeading.parse(child, typography, colors)?.let(children::add)
                    is Paragraph -> children.add(MDParagraph.parse(child, typography, colors))
                }

                child = child.next
            }

            return MDBlock(children)
        }
    }

    @Composable
    override fun render() {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            for ((index, child) in children.withIndex()) {
                key(index) {
                    child.render()
                }
            }
        }
    }

    override fun toString(): String {
        return "MDBlock(children=${children.joinToString(",\n", "[\n", "\n]")})"
    }
}
