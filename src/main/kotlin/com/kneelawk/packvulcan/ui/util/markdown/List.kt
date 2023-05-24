package com.kneelawk.packvulcan.ui.util.markdown

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.unit.dp
import com.vladsch.flexmark.ast.BulletList
import com.vladsch.flexmark.ast.BulletListItem
import com.vladsch.flexmark.ast.Paragraph

class MDBulletList(private val children: List<List<MDNode>>) : MDNode {
    companion object {
        fun parse(list: BulletList, ctx: MDContext): MDBulletList {
            var child = list.firstChild
            val children = mutableListOf<List<MDNode>>()

            while (child != null) {
                if (child is BulletListItem) {
                    val itemChildren = mutableListOf<MDNode>()

                    var itemChild = child.firstChild
                    while (itemChild != null) {
                        when (itemChild) {
                            is Paragraph -> itemChildren.add(MDParagraph.parse(itemChild, ctx))
                            is BulletList -> itemChildren.add(parse(itemChild, ctx))
                        }

                        itemChild = itemChild.next
                    }

                    children.add(itemChildren)
                }

                child = child.next
            }

            return MDBulletList(children)
        }
    }

    @Composable
    override fun render() {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            for ((index, child) in children.withIndex()) {
                key(index) {
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("\u2022")

                        Column {
                            for ((subIndex, subChild) in child.withIndex()) {
                                key(subIndex) {
                                    subChild.render()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun toString(): String {
        return "MDBulletList(children=${children.joinToString(",\n", "[\n", "\n]")})"
    }
}
