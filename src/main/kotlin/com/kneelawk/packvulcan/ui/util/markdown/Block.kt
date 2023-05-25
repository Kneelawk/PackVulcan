package com.kneelawk.packvulcan.ui.util.markdown

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.unit.dp
import com.vladsch.flexmark.ast.*
import com.vladsch.flexmark.util.ast.ContentNode
import com.vladsch.flexmark.util.ast.Node

class MDBlock(private val children: List<MDNode>) : MDNode {
    companion object {
        fun parse(node: Node, ctx: MDContext): MDBlock {
            var child = node.firstChild
            val children = mutableListOf<MDNode>()

            println("Block [")
            while (child != null) {
                println("  $child")
                when (child) {
                    is Heading -> MDHeading.parse(child, ctx)?.let(children::add)
                    is Paragraph -> children.add(MDParagraph.parse(child, ctx))
                    is HtmlBlock -> MDHTML.parseBlock(child, ctx)
                        ?.let(children::add)
                    is ThematicBreak -> children.add(MDThematicBreak)
                    is BulletList -> children.add(MDBulletList.parse(child, ctx))
                    is CodeBlock, is FencedCodeBlock, is IndentedCodeBlock -> children.add(
                        MDCodeBlock.parse(child as ContentNode)
                    )
                }

                child = child.next
            }
            println("]")

            return MDBlock(children)
        }
    }

    @Composable
    override fun render() {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
