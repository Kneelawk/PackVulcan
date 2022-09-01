package com.kneelawk.packvulcan.ui.util.widgets

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
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
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    colors: TextFieldColors = TextFieldDefaults.textFieldColors(unfocusedIndicatorColor = Color.Transparent),
    shape: Shape = MaterialTheme.shapes.small,
    isError: Boolean = false,
    fontSize: TextUnit = MaterialTheme.typography.body1.fontSize,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    permanentIcon: @Composable () -> Unit = {},
    ghostText: @Composable () -> Unit = {}
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
            minWidth = SmallTextFieldDefaults.MinWidth,
            minHeight = SmallTextFieldDefaults.MinHeight
        ).background(colors.backgroundColor(enabled).value, shape)
            .border(BorderStroke(borderThickness, borderColor), shape),
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        textStyle = LocalTextStyle.current.copy(
            color = MaterialTheme.colors.onSurface,
            fontSize = fontSize
        ),
        cursorBrush = SolidColor(colors.cursorColor(isError).value),
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            Row(
                Modifier.padding(horizontal = SmallTextFieldDefaults.HorizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                permanentIcon()

                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        CompositionLocalProvider(LocalContentAlpha provides 0.5f) {
                            ghostText()
                        }
                    }

                    innerTextField()
                }
            }
        }
    )
}

object SmallTextFieldDefaults {
    val HorizontalPadding = 12.dp

    val MinWidth = 64.dp
    val MinHeight = 36.dp
}
