package com.kneelawk.packvulcan.ui.util.widgets

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

@Composable
fun TextArea(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    colors: TextFieldColors = TextFieldDefaults.textFieldColors(unfocusedIndicatorColor = Color.Transparent),
    shape: Shape = MaterialTheme.shapes.small,
    isError: Boolean = false,
    fontSize: TextUnit = MaterialTheme.typography.body1.fontSize,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    scrollState: ScrollState = rememberScrollState()
) {
    val isFocused by interactionSource.collectIsFocusedAsState()

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
        modifier = modifier.defaultMinSize(minWidth = 64.dp, minHeight = 36.dp)
            .background(colors.backgroundColor(enabled).value, shape)
            .border(BorderStroke(borderThickness, borderColor), shape)
    ) {
        Column(modifier = Modifier.verticalScroll(scrollState).weight(1f).fillMaxHeight()) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = false,
                enabled = enabled,
                readOnly = readOnly,
                textStyle = LocalTextStyle.current.copy(
                    color = MaterialTheme.colors.onSurface,
                    fontSize = fontSize
                ),
                cursorBrush = SolidColor(colors.cursorColor(isError).value),
                interactionSource = interactionSource,
                decorationBox = { innerTextField ->
                    Row(
                        Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        innerTextField()
                    }
                }
            )
        }

        VerticalScrollbar(
            rememberScrollbarAdapter(scrollState),
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 10.dp).fillMaxHeight()
        )
    }
}
