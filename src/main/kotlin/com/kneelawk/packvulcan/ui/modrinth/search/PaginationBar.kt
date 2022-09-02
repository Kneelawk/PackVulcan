package com.kneelawk.packvulcan.ui.modrinth.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kneelawk.packvulcan.ui.util.widgets.SmallButton

@Composable
fun PaginationBar(controller: ModrinthSearchInterface) {
    Row(
        Modifier.padding(top = 10.dp, end = 20.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SmallButton(
            onClick = { controller.pageBackward() },
            minWidth = ButtonDefaults.MinHeight,
            colors = ButtonDefaults.buttonColors(MaterialTheme.colors.secondary),
            enabled = controller.currentPage > 1
        ) {
            Text("<")
        }

        if (controller.finalPage <= 7) {
            for (i in 1..controller.finalPage) {
                val background =
                    if (i == controller.currentPage) MaterialTheme.colors.primary else MaterialTheme.colors.secondary

                SmallButton(
                    onClick = { controller.goToPage(i) },
                    minWidth = ButtonDefaults.MinHeight,
                    colors = ButtonDefaults.buttonColors(backgroundColor = background)
                ) {
                    Text("$i")
                }
            }
        } else if (controller.currentPage < 5) {
            for (i in 1..5) {
                val background =
                    if (i == controller.currentPage) MaterialTheme.colors.primary else MaterialTheme.colors.secondary

                SmallButton(
                    onClick = { controller.goToPage(i) },
                    minWidth = ButtonDefaults.MinHeight,
                    colors = ButtonDefaults.buttonColors(backgroundColor = background)
                ) {
                    Text("$i")
                }
            }

            Text("\u2014", modifier = Modifier.padding(horizontal = 10.dp))

            SmallButton(
                onClick = { controller.goToPage(controller.finalPage) },
                minWidth = ButtonDefaults.MinHeight,
                colors = ButtonDefaults.buttonColors(MaterialTheme.colors.secondary)
            ) {
                Text("${controller.finalPage}")
            }
        } else if (controller.currentPage > controller.finalPage - 4) {
            SmallButton(
                onClick = { controller.goToPage(1) },
                minWidth = ButtonDefaults.MinHeight,
                colors = ButtonDefaults.buttonColors(MaterialTheme.colors.secondary)
            ) {
                Text("1")
            }

            Text("\u2014", modifier = Modifier.padding(horizontal = 10.dp))

            for (i in 1..5) {
                val page = controller.finalPage - 5 + i
                val background =
                    if (page == controller.currentPage) MaterialTheme.colors.primary else MaterialTheme.colors.secondary

                SmallButton(
                    onClick = { controller.goToPage(page) },
                    minWidth = ButtonDefaults.MinHeight,
                    colors = ButtonDefaults.buttonColors(backgroundColor = background)
                ) {
                    Text("$page")
                }
            }
        } else {
            SmallButton(
                onClick = { controller.goToPage(1) },
                minWidth = ButtonDefaults.MinHeight,
                colors = ButtonDefaults.buttonColors(MaterialTheme.colors.secondary)
            ) {
                Text("1")
            }

            Text("\u2014", modifier = Modifier.padding(horizontal = 10.dp))

            SmallButton(
                onClick = { controller.pageBackward() },
                minWidth = ButtonDefaults.MinHeight,
                colors = ButtonDefaults.buttonColors(MaterialTheme.colors.secondary)
            ) {
                Text("${controller.currentPage - 1}")
            }

            SmallButton(
                onClick = { controller.goToPage(controller.currentPage) },
                minWidth = ButtonDefaults.MinHeight
            ) {
                Text("${controller.currentPage}")
            }

            SmallButton(
                onClick = { controller.pageForward() },
                minWidth = ButtonDefaults.MinHeight,
                colors = ButtonDefaults.buttonColors(MaterialTheme.colors.secondary)
            ) {
                Text("${controller.currentPage + 1}")
            }

            Text("\u2014", modifier = Modifier.padding(horizontal = 10.dp))

            SmallButton(
                onClick = { controller.goToPage(controller.finalPage) },
                minWidth = ButtonDefaults.MinHeight,
                colors = ButtonDefaults.buttonColors(MaterialTheme.colors.secondary)
            ) {
                Text("${controller.finalPage}")
            }
        }

        SmallButton(
            onClick = { controller.pageForward() },
            minWidth = ButtonDefaults.MinHeight,
            colors = ButtonDefaults.buttonColors(MaterialTheme.colors.secondary),
            enabled = controller.currentPage < controller.finalPage
        ) {
            Text(">")
        }
    }
}
