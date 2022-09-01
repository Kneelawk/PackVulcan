/*
 * Copyright 2019 The Android Open Source Project
 *
 * Modifications by Kneelawk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kneelawk.packvulcan.ui.util.widgets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object SmallButtonDefaults {
    val HorizontalPadding = 12.dp
    val VerticalPadding = 6.dp

    /**
     * The default content padding used by [SmallButton]
     */
    val ContentPadding = PaddingValues(
        start = HorizontalPadding,
        top = VerticalPadding,
        end = HorizontalPadding,
        bottom = VerticalPadding
    )

    /**
     * The default min width applied for the [SmallButton].
     * Note that you can override it by applying Modifier.widthIn directly on [SmallButton].
     */
    val MinWidth = 64.dp

    /**
     * The default min height applied for the [SmallButton].
     * Note that you can override it by applying Modifier.heightIn directly on [SmallButton].
     */
    val MinHeight = 36.dp

    /**
     * The default size of the icon when used inside a [SmallButton].
     */
    val IconSize = 24.dp

    private val TextButtonHorizontalPadding = 8.dp

    /**
     * The default content padding used by [SmallTextButton]
     */
    val TextButtonContentPadding = PaddingValues(
        start = TextButtonHorizontalPadding,
        top = ContentPadding.calculateTopPadding(),
        end = TextButtonHorizontalPadding,
        bottom = ContentPadding.calculateBottomPadding()
    )

    val IconButtonPadding = PaddingValues(VerticalPadding)
}

@Composable
fun ListButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    elevation: ButtonElevation? = null,
    shape: Shape = MaterialTheme.shapes.small,
    border: BorderStroke? = null,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    contentPadding: PaddingValues = SmallButtonDefaults.ContentPadding,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable RowScope.() -> Unit
) {
    val contentColor by colors.contentColor(enabled)
    SmallSurface(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        color = colors.backgroundColor(enabled).value,
        contentColor = contentColor.copy(alpha = 1f),
        border = border,
        elevation = elevation?.elevation(enabled, interactionSource)?.value ?: 0.dp,
        interactionSource = interactionSource
    ) {
        CompositionLocalProvider(LocalContentAlpha provides contentColor.alpha) {
            ProvideTextStyle(
                value = MaterialTheme.typography.button
            ) {
                Row(
                    Modifier
                        .defaultMinSize(
                            minWidth = SmallButtonDefaults.MinWidth,
                            minHeight = SmallButtonDefaults.MinHeight
                        )
                        .padding(contentPadding),
                    horizontalArrangement = horizontalArrangement,
                    verticalAlignment = verticalAlignment,
                    content = content
                )
            }
        }
    }
}

@Composable
fun ListButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    elevation: ButtonElevation? = null,
    shape: Shape = MaterialTheme.shapes.small,
    border: BorderStroke? = null,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    icon: (@Composable () -> Unit)? = null,
    text: String,
) {
    ListButton(onClick, modifier, enabled, interactionSource, elevation, shape, border, colors) {
        if (icon != null) {
            icon()
            Text(text, modifier = Modifier.padding(start = 10.dp))
        } else {
            Text(text)
        }
    }
}

@Composable
fun CheckboxButton(
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    elevation: ButtonElevation? = null,
    shape: Shape = MaterialTheme.shapes.small,
    border: BorderStroke? = null,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    checkboxColors: CheckboxColors = CheckboxDefaults.colors(MaterialTheme.colors.primary),
    icon: (@Composable () -> Unit)? = null,
    text: String,
) {
    ListButton(onClick, modifier, enabled, interactionSource, elevation, shape, border, colors) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            modifier = Modifier.padding(end = 10.dp),
            enabled = enabled,
            interactionSource = interactionSource,
            colors = checkboxColors
        )
        if (icon != null) {
            icon()
            Text(text, modifier = Modifier.padding(start = 10.dp))
        } else {
            Text(text)
        }
    }
}

@Composable
fun SmallButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    elevation: ButtonElevation? = ButtonDefaults.elevation(),
    shape: Shape = MaterialTheme.shapes.small,
    border: BorderStroke? = null,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = SmallButtonDefaults.ContentPadding,
    minWidth: Dp = SmallButtonDefaults.MinWidth,
    minHeight: Dp = SmallButtonDefaults.MinHeight,
    content: @Composable RowScope.() -> Unit
) {
    val contentColor by colors.contentColor(enabled)
    SmallSurface(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        color = colors.backgroundColor(enabled).value,
        contentColor = contentColor.copy(alpha = 1f),
        border = border,
        elevation = elevation?.elevation(enabled, interactionSource)?.value ?: 0.dp,
        interactionSource = interactionSource
    ) {
        CompositionLocalProvider(LocalContentAlpha provides contentColor.alpha) {
            ProvideTextStyle(
                value = MaterialTheme.typography.button
            ) {
                Row(
                    Modifier
                        .defaultMinSize(minWidth, minHeight)
                        .padding(contentPadding),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    content = content
                )
            }
        }
    }
}

@Composable
fun SmallTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    elevation: ButtonElevation? = null,
    shape: Shape = MaterialTheme.shapes.small,
    border: BorderStroke? = null,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    contentPadding: PaddingValues = SmallButtonDefaults.TextButtonContentPadding,
    content: @Composable RowScope.() -> Unit
) = SmallButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    interactionSource = interactionSource,
    elevation = elevation,
    shape = shape,
    border = border,
    colors = colors,
    contentPadding = contentPadding,
    content = content
)

@Composable
fun SmallIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = SmallButtonDefaults.IconButtonPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    size: Dp = SmallButtonDefaults.MinHeight,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clickable(
                onClick = onClick,
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = false, radius = size / 2)
            )
            .defaultMinSize(
                minWidth = size,
                minHeight = size
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        val contentAlpha = if (enabled) LocalContentAlpha.current else ContentAlpha.disabled
        CompositionLocalProvider(LocalContentAlpha provides contentAlpha, content = content)
    }
}
