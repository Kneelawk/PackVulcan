package com.kneelawk.mrmpb.ui.util.widgets

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
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
fun SmallTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    colors: TextFieldColors = TextFieldDefaults.textFieldColors(unfocusedIndicatorColor = Color.Transparent),
    shape: Shape = MaterialTheme.shapes.small,
    isError: Boolean = false,
    fontSize: TextUnit = MaterialTheme.typography.body1.fontSize,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
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

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.defaultMinSize(
            minWidth = 64.dp,
            minHeight = 36.dp
        ).background(colors.backgroundColor(enabled).value, shape)
            .border(BorderStroke(borderThickness, borderColor), shape),
        singleLine = singleLine,
        textStyle = LocalTextStyle.current.copy(
            color = MaterialTheme.colors.onSurface,
            fontSize = fontSize
        ),
        cursorBrush = SolidColor(colors.cursorColor(isError).value),
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            Row(
                Modifier.padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                innerTextField()
            }
        }
    )
}
