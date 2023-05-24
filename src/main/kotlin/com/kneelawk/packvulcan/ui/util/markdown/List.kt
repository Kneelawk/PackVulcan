package com.kneelawk.packvulcan.ui.util.markdown

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Colors
import androidx.compose.material.Text
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.unit.dp
import com.kneelawk.packvulcan.ui.theme.PackVulcanColors
import com.vladsch.flexmark.ast.BulletList
import com.vladsch.flexmark.ast.BulletListItem
import com.vladsch.flexmark.ast.Paragraph

class MDBulletList(private val children: List<MDNode>) : MDNode {
    companion object {
        fun parse(list: BulletList, typography: Typography, colors: Colors, pvColors: PackVulcanColors): MDBulletList {
            var child = list.firstChild
            val children = mutableListOf<MDNode>()

            while (child != null) {
                if (child is BulletListItem) {
                    val childChild = child.firstChild
                    if (childChild is Paragraph) {
                        children.add(MDParagraph.parse(childChild, typography, colors, pvColors))
                    }
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

                        child.render()
                    }
                }
            }
        }
    }

    override fun toString(): String {
        return "MDBulletList(children=${children.joinToString(",\n", "[\n", "\n]")})"
    }
}
