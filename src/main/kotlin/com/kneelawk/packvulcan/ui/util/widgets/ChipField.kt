package com.kneelawk.packvulcan.ui.util.widgets

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.awtEventOrNull
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import java.awt.event.KeyEvent

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChipField(
    text: String,
    onTextChange: (String) -> Unit,
    chips: List<String>,
    chipOptions: List<String>,
    addChip: (String) -> Unit,
    removeChip: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: TextFieldColors = TextFieldDefaults.textFieldColors(unfocusedIndicatorColor = Color.Transparent),
    shape: Shape = MaterialTheme.shapes.small,
    isError: Boolean = false,
    fontSize: TextUnit = MaterialTheme.typography.body1.fontSize,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    textFocusRequester: FocusRequester = remember { FocusRequester() },
) {
    val isFocused by interactionSource.collectIsFocusedAsState()
    var dropDownOpen by remember { mutableStateOf(false) }

    val borderThicknessState by remember {
        derivedStateOf {
            if (isFocused) {
                2.dp
            } else {
                1.dp
            }
        }
    }

    val borderThickness by animateDpAsState(borderThicknessState)
    val borderColor by colors.indicatorColor(enabled, isError, interactionSource)

    Row(
        modifier = modifier.background(colors.backgroundColor(enabled).value, shape)
            .defaultMinSize(SmallTextFieldDefaults.MinWidth, SmallTextFieldDefaults.MinHeight)
            .border(BorderStroke(borderThickness, borderColor), shape)
            .padding(horizontal = 2.dp)
            .onFocusChanged {
                if (!it.hasFocus) {
                    dropDownOpen = false
                }
            }
            .onKeyEvent {
                if (it.type == KeyEventType.KeyDown) {
                    val keyCode = it.awtEventOrNull?.keyCode
                    if (keyCode == KeyEvent.VK_ESCAPE) {
                        dropDownOpen = false
                        true
                    } else false
                } else false
            },
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (chip in chips) {
            key(chip) {
                val chipFocusRequester = remember { FocusRequester() }
                var isChipFocused by remember { mutableStateOf(false) }

                SmallFilterChip(
                    selected = isChipFocused,
                    onClick = {
                        chipFocusRequester.requestFocus()
                    },
                    enabled = enabled,
                    modifier = Modifier.focusRequester(chipFocusRequester)
                        .onFocusChanged {
                            isChipFocused = it.isFocused
                        }
                        .focusable(enabled, interactionSource)
                        .onKeyEvent {
                            if (it.type == KeyEventType.KeyDown) {
                                val keyCode = it.awtEventOrNull?.keyCode
                                if (keyCode == KeyEvent.VK_BACK_SPACE || keyCode == KeyEvent.VK_DELETE) {
                                    removeChip(chip)
                                    true
                                } else false
                            } else false
                        },
                    trailingIcon = {
                        SmallIconButton(
                            onClick = { removeChip(chip) },
                            size = 18.dp,
                            enabled = enabled
                        ) {
                            Icon(Icons.Default.Close, "remove", modifier = Modifier.size(18.dp))
                        }
                    }
                ) {
                    Text(chip)
                }
            }
        }

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            BasicTextField(
                value = text,
                onValueChange = {
                    onTextChange(it)
                    dropDownOpen = true
                },
                modifier = Modifier.fillMaxWidth()
                    .defaultMinSize(
                        minWidth = SmallTextFieldDefaults.MinWidth,
                        minHeight = SmallTextFieldDefaults.MinHeight
                    )
                    .focusRequester(textFocusRequester)
                    .onKeyEvent {
                        if (it.type == KeyEventType.KeyDown) {
                            val keyCode = it.awtEventOrNull?.keyCode
                            if (keyCode == KeyEvent.VK_BACK_SPACE && text.isEmpty()) {
                                chips.lastOrNull()?.let(removeChip)
                                true
                            } else if (keyCode == KeyEvent.VK_ENTER && text.isNotEmpty()) {
                                addChip(text)
                                true
                            } else false
                        } else false
                    },
                singleLine = true,
                enabled = enabled,
                textStyle = LocalTextStyle.current.copy(
                    color = MaterialTheme.colors.onSurface,
                    fontSize = fontSize
                ),
                keyboardActions = KeyboardActions {
                    addChip(text)
                },
                cursorBrush = SolidColor(colors.cursorColor(isError).value),
                interactionSource = interactionSource,
                decorationBox = { innerTextField ->
                    Row(
                        Modifier.padding(
                            start = if (chips.isEmpty()) SmallTextFieldDefaults.HorizontalPadding else 0.dp,
                            end = SmallTextFieldDefaults.HorizontalPadding
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        innerTextField()
                    }
                }
            )

            LazyDropdownMenu(
                expanded = dropDownOpen,
                onDismissRequest = { dropDownOpen = false },
                focusable = false
            ) {
                items(chipOptions.filter { it.startsWith(text) }) { option ->
                    DropdownMenuItem(
                        onClick = {
                            addChip(option)
                        },
                        enabled = enabled
                    ) {
                        Text(option)
                    }
                }
            }
        }
    }
}
