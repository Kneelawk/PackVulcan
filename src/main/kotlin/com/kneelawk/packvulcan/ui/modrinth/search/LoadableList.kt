package com.kneelawk.packvulcan.ui.modrinth.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.kneelawk.packvulcan.ui.modrinth.DisplayElement
import com.kneelawk.packvulcan.ui.util.widgets.CheckboxButton
import com.kneelawk.packvulcan.ui.util.widgets.SmallTextButton

@Composable
fun <T : DisplayElement> StaticLoadableList(
    list: List<T>,
    selected: Map<T, Unit>,
    selectItem: (T) -> Unit,
    unselectItem: (T) -> Unit,
    enabled: Boolean, loading: Boolean
) {
    Box(contentAlignment = Alignment.Center) {
        Column {
            for (item in list) {
                key(item) {
                    CheckboxButton(
                        checked = selected.contains(item),
                        onClick = {
                            if (selected.contains(item)) {
                                unselectItem(item)
                            } else {
                                selectItem(item)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = enabled,
                        icon = {
                            item.icon?.icon(item.prettyName, Modifier.size(24.dp)) ?: Box(Modifier.size(24.dp))
                        },
                        text = item.prettyName
                    )
                }
            }
        }

        if (loading) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun <T : DisplayElement> CollapsableLoadableList(
    list: List<T>,
    selected: Map<T, Unit>,
    selectItem: (T) -> Unit,
    unselectItem: (T) -> Unit,
    itemAlwaysVisible: (T) -> Boolean,
    enabled: Boolean,
    loading: Boolean
) {
    var collapsed by remember { mutableStateOf(true) }

    Box(contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                for (item in list) {
                    key(item) {
                        AnimatedVisibility(visible = !collapsed || itemAlwaysVisible(item)) {
                            CheckboxButton(
                                checked = selected.contains(item),
                                onClick = {
                                    if (selected.contains(item)) {
                                        unselectItem(item)
                                    } else {
                                        selectItem(item)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = enabled,
                                icon = {
                                    item.icon?.icon(item.prettyName, Modifier.size(24.dp)) ?: Box(Modifier.size(24.dp))
                                },
                                text = item.prettyName
                            )
                        }
                    }
                }

                SmallTextButton(
                    onClick = {
                        collapsed = !collapsed
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val iconRotation by animateFloatAsState(if (collapsed) 0f else -180f)
                    Icon(
                        Icons.Default.ArrowDropDown, "drop-down",
                        modifier = Modifier.rotate(iconRotation)
                    )
                    Text("More", Modifier.padding(start = 5.dp))
                }
            }
        }

        if (loading) {
            CircularProgressIndicator()
        }
    }
}
