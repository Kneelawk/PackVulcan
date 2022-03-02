package com.kneelawk.mrmpb.ui.util

import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout

/**
 * A layout composable for creating form layouts. This is similar to a grid, but with some modifications.
 */
@Composable
inline fun Form(
    modifier: Modifier = Modifier,
    columnWeights: FloatArray,
    content: @Composable FormScope.() -> Unit
) {
//    Layout(
//        content = { FormScopeInstance.content() },
//        modifier = modifier,
//        measurePolicy = { measurables, constraints ->
//            layout()
//        }
//    )
}

/**
 * Scope for the children of [Form].
 */
@LayoutScopeMarker
@Immutable
interface FormScope {

}

internal object FormScopeInstance : FormScope {

}
