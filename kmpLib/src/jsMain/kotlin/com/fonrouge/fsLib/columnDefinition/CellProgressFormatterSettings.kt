package com.fonrouge.fsLib.columnDefinition

import js.objects.jso

/**
 * Represents settings for the progress formatter in a column definition.
 *
 * https://tabulator.info/docs/6.3/format#formatter-progress
 *
 * These settings allow customization of how progress bars are displayed in a table column,
 * including minimum/maximum values, colors, legends, and alignment.
 *
 * @property min Specifies the minimum value for the progress range.
 * @property max Specifies the maximum value for the progress range.
 * @property color Defines the color of the progress bar. Can accept different types such as strings, arrays, or functional assignments.
 * @property colorAsString (unused) A string representation of the progress bar color.
 * @property colorAsArray (unused) An array of color values for the progress bar.
 * @property colorAsFunction (unused) A function that determines the progress bar color based on a numeric value.
 * @property legend A function that defines the textual representation of the progress value.
 * @property legendColor Defines the color of the text used in the legend. Accepts multiple representations like strings, arrays, or functional assignments.
 * @property legendColorAsString (unused) A string representation of the legend color.
 * @property legendColorAsArray (unused) An array of color values for the legend.
 * @property legendColorAsFunction (unused) A function that determines the legend color based on a numeric value.
 * @property legendAlign Specifies the alignment of the legend using the `LegendAlign` enum.
 */
external interface CellProgressFormatterSettings {
    var min: Int
    var max: Int
    var color: Any
    var colorAsString: String
    var colorAsArray: Array<String>
    var colorAsFunction: (Double) -> String
    var legend: (Double) -> String
    var legendColor: Any
    var legendColorAsString: String
    var legendColorAsArray: Array<String>
    var legendColorAsFunction: (Double) -> String
    var legendAlign: LegendAlign
}

@Suppress("unused", "EnumEntryName")
enum class LegendAlign {
    center, left, right, justify
}

/**
 * Defines settings for the progress formatter in a column definition.
 *
 * This function provides an instance of [CellProgressFormatterSettings] which can be
 * used to customize the display of progress bars in a table column, including properties
 * such as minimum/maximum values, color settings, legends, and alignment options.
 *
 * @return An instance of [CellProgressFormatterSettings] for configuring progress formatter settings.
 */
@Suppress("unused")
fun cellProgressFormatterSettings(block: CellProgressFormatterSettings.() -> Unit) = jso(block)
