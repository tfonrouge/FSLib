package com.fonrouge.fsLib.columnDefinition

import js.objects.jso

/**
 * Represents the parameters required for formatting a cell with a progress indicator.
 *
 * The `CellProgressFormatterParams` interface is used to configure the behavior and appearance of
 * progress indicators within table cells, including the range, colors, and legend properties.
 *
 * @property min The minimum value of the progress range.
 * @property max The maximum value of the progress range.
 * @property color Defines the color of the progress bar. This can be a single color, an array of colors,
 * or a function to dynamically determine the color based on the progress value.
 * @property colorAsString Represents the color as a string value, if applicable.
 * @property colorAsArray Represents the color as an array of string values, if applicable.
 * @property colorAsFunction A function that calculates the color of the progress bar based on a progress value.
 * @property legend Represents the legend associated with the progress bar, providing contextual information.
 * @property legendColor Defines the color used for the legend. This can be a single color, an array of colors,
 * or a function to dynamically determine the color based on the associated value.
 * @property legendColorAsString Represents the legend color as a string value, if applicable.
 * @property legendColorAsArray Represents the legend color as an array of string values, if applicable.
 * @property legendColorAsFunction A function that determines the legend color dynamically based on a value.
 * @property legendAlign Specifies the alignment of the legend text. Options include `center`, `left`, `right`, or `justify`.
 */
external interface CellProgressFormatterParams {
    var min: Number
    var max: Number
    var color: Any
    var colorAsString: String?
    var colorAsArray: Array<String>?
    var colorAsFunction: ((Double) -> String)?
    var legend: Any
    var legendColor: Any
    var legendColorAsString: String?
    var legendColorAsArray: Array<String>?
    var legendColorAsFunction: ((Double) -> String)?
    var legendAlign: LegendAlign
}

@Suppress("unused", "EnumEntryName")
enum class LegendAlign {
    center, left, right, justify
}

/**
 * Configures and returns the parameters for a progress formatter in a table cell.
 *
 * This function creates an instance of `CellProgressFormatterParams` and applies the provided configuration
 * using the lambda function. The configuration supports customizing properties such as minimum/maximum values,
 * colors, legend text, and alignment. If specific color-related properties are set (`colorAsString`,
 * `colorAsArray`, `colorAsFunction`, etc.), they overwrite the generic `color` property. Similarly, legend color
 * properties overwrite the `legendColor` property.
 *
 * @param block A lambda function used to configure the properties of the `CellProgressFormatterParams` object.
 * The lambda may define properties such as `min` (minimum value of the progress bar range), `max` (maximum value of the range),
 * `color` (progress bar color), `legend` (progress value legend), `legendAlign` (alignment of the legend),
 * and other color/legend customization options like `colorAsString` or `legendColorAsArray`.
 * @return A `CellProgressFormatterParams` object containing the configured progress formatter settings.
 */
@Suppress("unused")
fun cellProgressFormatterParams(block: CellProgressFormatterParams.() -> Unit): CellProgressFormatterParams {
    val r: CellProgressFormatterParams = jso(block)
    r.colorAsString?.let { r.color = it }
    r.colorAsArray?.let { r.color = it }
    r.colorAsFunction?.let { r.color = it }
    r.legendColorAsString?.let { r.legendColor = it }
    r.legendColorAsArray?.let { r.legendColor = it }
    r.legendColorAsFunction?.let { r.legendColor = it }
    return r
}
