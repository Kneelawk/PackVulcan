package com.kneelawk.packvulcan.ui.modrinth.search

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.kneelawk.packvulcan.ui.util.widgets.SmallButton
import java.time.Duration
import java.time.Instant

private val DROPDOWN_WAIT = Duration.ofMillis(200)

@Composable
fun <T> OptionsDropDown(
    options: Array<T>, buttonText: String, selectOption: (T) -> Unit, menuItem: @Composable RowScope.(T) -> Unit
) {
    Box {
        var expanded by remember { mutableStateOf(false) }
        var expandTimer by remember { mutableStateOf(Instant.now()) }

        SmallButton(
            onClick = {
                if (expanded) {
                    expanded = false
                } else if (Duration.between(expandTimer, Instant.now()) >= DROPDOWN_WAIT) {
                    expanded = true
                }
            },
            colors = ButtonDefaults.buttonColors(MaterialTheme.colors.secondary)
        ) {
            Text(buttonText, modifier = Modifier.padding(end = 5.dp))
            val iconRotation by animateFloatAsState(if (expanded) 180f else 0f)
            Icon(Icons.Default.ArrowDropDown, "drop-down", modifier = Modifier.rotate(iconRotation))
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                expandTimer = Instant.now()
            }
        ) {
            for (option in options) {
                key(option) {
                    DropdownMenuItem(
                        onClick = {
                            expanded = false
                            selectOption(option)
                        }
                    ) {
                        menuItem(option)
                    }
                }
            }
        }
    }
}
