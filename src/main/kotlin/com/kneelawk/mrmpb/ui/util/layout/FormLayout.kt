package com.kneelawk.mrmpb.ui.util.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.*
import androidx.compose.ui.unit.*

/**
 * A layout composable for creating form layouts. This is similar to a grid, but with some modifications.
 */
@Composable
inline fun Form(
    modifier: Modifier = Modifier,
    sectionHeaderAlignment: Alignment.Horizontal = Alignment.Start,
    labelAlignment: Alignment = Alignment.CenterStart,
    fieldAlignment: Alignment.Vertical = Alignment.CenterVertically,
    configureAlignment: Alignment = Alignment.CenterEnd,
    rowArrangement: Arrangement.Vertical = Arrangement.Top,
    columnSpacing: Dp = 0.dp,
    content: @Composable FormScope.() -> Unit
) {
    val measurePolicy = remember(
        sectionHeaderAlignment, labelAlignment, fieldAlignment, configureAlignment, rowArrangement, columnSpacing
    ) {
        FormMeasurePolicy(
            sectionHeaderAlignment, labelAlignment, fieldAlignment, configureAlignment, rowArrangement, columnSpacing
        )
    }
    Layout(
        content = { FormScopeInstance.content() },
        modifier = modifier,
        measurePolicy = measurePolicy
    )
}

/**
 * Scope for the children of [Form].
 */
@LayoutScopeMarker
@Immutable
interface FormScope {
    fun Modifier.formSection(): Modifier

    fun Modifier.formLabel(): Modifier

    fun Modifier.formField(): Modifier

    fun Modifier.formConfigure(): Modifier
}

/*
 * == Implementation ==
 */

object FormScopeInstance : FormScope {
    override fun Modifier.formSection() = this.then(FormParentData.SECTION)

    override fun Modifier.formLabel() = this.then(FormParentData.LABEL)

    override fun Modifier.formField() = this.then(FormParentData.FIELD)

    override fun Modifier.formConfigure() = this.then(FormParentData.CONFIGURE)
}

private enum class FormParentData : ParentDataModifier {
    SECTION, LABEL, FIELD, CONFIGURE;

    override fun Density.modifyParentData(parentData: Any?): Any? = this@FormParentData
}

private sealed class MeasurableFormLine {
    data class SectionHeader(val header: Measurable) : MeasurableFormLine()
    data class FieldLine(val label: Measurable?, val field: Measurable?, val configure: Measurable?) :
        MeasurableFormLine()
}

private sealed class PlaceableFormLine {
    abstract val height: Int

    data class SectionHeader(val header: Placeable) : PlaceableFormLine() {
        override val height = header.height
    }

    data class FieldLine(val label: Placeable?, val field: Placeable?, val configure: Placeable?) :
        PlaceableFormLine() {
        override val height by lazy {
            maxOf(
                label?.height ?: throw IllegalStateException("label is null"),
                field?.height ?: throw IllegalStateException("field is null"), configure?.height ?: 0
            )
        }
    }
}

class FormMeasurePolicy(
    private val sectionHeaderAlignment: Alignment.Horizontal, private val labelAlignment: Alignment,
    private val fieldAlignment: Alignment.Vertical, private val configureAlignment: Alignment,
    private val rowArrangement: Arrangement.Vertical, private val columnSpacing: Dp
) : MeasurePolicy {
    override fun MeasureScope.measure(measurables: List<Measurable>, constraints: Constraints): MeasureResult {
        // Collect the measurables into form lines
        val measurableFormLines = mutableListOf<MeasurableFormLine>()
        var currentFieldLine: MeasurableFormLine.FieldLine? = null

        for (child in measurables) {
            val parentData = child.parentData as? FormParentData ?: throw IllegalStateException(
                "Child $child missing form parent data. Please supply a modifier of one of: formSection(), formLabel(), formField(), or formConfigure()"
            )
            when (parentData) {
                FormParentData.SECTION -> {
                    // complete the previous field line
                    if (currentFieldLine != null) {
                        measurableFormLines.add(currentFieldLine)
                        currentFieldLine = null
                    }
                    measurableFormLines.add(MeasurableFormLine.SectionHeader(child))
                }
                FormParentData.LABEL -> {
                    currentFieldLine = if (currentFieldLine != null) {
                        if (currentFieldLine.label != null) {
                            // the field line is from a previous line and needs to be completed
                            measurableFormLines.add(currentFieldLine)
                            MeasurableFormLine.FieldLine(child, null, null)
                        } else {
                            currentFieldLine.copy(label = child)
                        }
                    } else {
                        MeasurableFormLine.FieldLine(child, null, null)
                    }
                }
                FormParentData.FIELD -> {
                    currentFieldLine = if (currentFieldLine != null) {
                        if (currentFieldLine.field != null) {
                            // the field line is from a previous line and needs to be completed
                            measurableFormLines.add(currentFieldLine)
                            MeasurableFormLine.FieldLine(null, child, null)
                        } else {
                            currentFieldLine.copy(field = child)
                        }
                    } else {
                        MeasurableFormLine.FieldLine(null, child, null)
                    }
                }
                FormParentData.CONFIGURE -> {
                    currentFieldLine = if (currentFieldLine != null) {
                        if (currentFieldLine.configure != null) {
                            // the field line is from a previous line and needs to be completed
                            measurableFormLines.add(currentFieldLine)
                            MeasurableFormLine.FieldLine(null, null, child)
                        } else {
                            currentFieldLine.copy(configure = child)
                        }
                    } else {
                        MeasurableFormLine.FieldLine(null, null, child)
                    }
                }
            }
        }

        // Complete the final field line
        if (currentFieldLine != null) {
            measurableFormLines.add(currentFieldLine)
        }

        val columnSpacingPx = columnSpacing.roundToPx()

        val formLines = arrayOfNulls<PlaceableFormLine>(measurableFormLines.size)

        // Measure label column and section headers
        var sectionCount = 0
        var lineCount = 0
        var labelWidth = 0
        for (i in measurableFormLines.indices) {
            when (val line = measurableFormLines[i]) {
                is MeasurableFormLine.FieldLine -> {
                    // Measure the label
                    val labelMeasurable = line.label ?: throw IllegalStateException(
                        "Form line $i missing label. Please provide a label by supplying a Modifier of formLabel() to one of this line's contents."
                    )
                    val label = labelMeasurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
                    formLines[i] = PlaceableFormLine.FieldLine(label, null, null)
                    lineCount++
                    labelWidth = maxOf(labelWidth, label.width)
                }
                is MeasurableFormLine.SectionHeader -> {
                    // Measure section headers here too
                    val header = line.header.measure(constraints.copy(minWidth = 0, minHeight = 0))
                    formLines[i] = PlaceableFormLine.SectionHeader(header)
                    sectionCount++
                }
            }
        }

        // Measure the configure widgets
        var configureWidth = 0
        for (i in measurableFormLines.indices) {
            val line = measurableFormLines[i]
            if (line is MeasurableFormLine.FieldLine) {
                // Measure the configure widget if it exists
                if (line.configure != null) {
                    val configure = line.configure.measure(constraints.copy(minWidth = 0, minHeight = 0))
                    // should always be a FieldLine
                    val oldLine = formLines[i] as PlaceableFormLine.FieldLine
                    formLines[i] = oldLine.copy(configure = configure)
                    configureWidth = maxOf(configureWidth, configure.width)
                }
            }
        }

        // Measure the fields
        val field1Width = constraints.maxWidth - labelWidth - columnSpacingPx - configureWidth - columnSpacingPx
        val field2Width = constraints.maxWidth - labelWidth - columnSpacingPx
        for (i in measurableFormLines.indices) {
            val line = measurableFormLines[i]
            if (line is MeasurableFormLine.FieldLine) {
                val fieldMeasurable = line.field ?: throw IllegalStateException(
                    "Form line $i missing field. Please provide a field by supplying a Modifier of formField() to one of this line's contents."
                )

                val maxWidth = if (line.configure != null) {
                    field1Width
                } else {
                    field2Width
                }

                // force the field to be the correct width
                val field =
                    fieldMeasurable.measure(constraints.copy(minHeight = 0, minWidth = maxWidth, maxWidth = maxWidth))
                // should always be a FieldLine
                val oldLine = formLines[i] as PlaceableFormLine.FieldLine
                formLines[i] = oldLine.copy(field = field)
            }
        }

        // Layout the components
        val layoutWidth = constraints.maxWidth
        val layoutHeight = maxOf(
            formLines.sumOf { it!!.height } + (rowArrangement.spacing * (sectionCount + lineCount - 1)).roundToPx(),
            constraints.minHeight)

        return layout(layoutWidth, layoutHeight) {
            val lineHeights = IntArray(formLines.size) { index ->
                formLines[index]!!.height
            }
            val linePositions = IntArray(formLines.size) { 0 }
            with(rowArrangement) { arrange(layoutHeight, lineHeights, linePositions) }

            formLines.forEachIndexed { index, line ->
                line!!
                val lineY = linePositions[index]

                when (line) {
                    is PlaceableFormLine.FieldLine -> {
                        val label = line.label!!
                        val labelPosition = labelAlignment.align(
                            IntSize(label.width, label.height), IntSize(labelWidth, line.height), LayoutDirection.Ltr
                        )
                        label.place(labelPosition.x, lineY + labelPosition.y)

                        val field = line.field!!
                        val fieldPosition = fieldAlignment.align(field.height, line.height)
                        field.place(labelWidth + columnSpacingPx, lineY + fieldPosition)

                        line.configure?.let { configure ->
                            val configurePosition = configureAlignment.align(
                                IntSize(configure.width, configure.height), IntSize(configureWidth, line.height),
                                LayoutDirection.Ltr
                            )
                            configure.place(
                                constraints.maxWidth - configureWidth + configurePosition.x, lineY + configurePosition.y
                            )
                        }
                    }
                    is PlaceableFormLine.SectionHeader -> {
                        val header = line.header
                        val headerPosition =
                            sectionHeaderAlignment.align(header.width, constraints.maxWidth, LayoutDirection.Ltr)
                        header.place(headerPosition, lineY)
                    }
                }
            }
        }
    }
}
