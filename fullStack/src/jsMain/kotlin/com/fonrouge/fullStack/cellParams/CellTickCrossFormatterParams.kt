package com.fonrouge.fullStack.cellParams

import js.objects.unsafeJso

/**
 * Represents configuration parameters for a cell tick/cross formatter.
 *
 * This interface defines properties to control the behavior and appearance of a tick/cross formatter,
 * which is often used in table columns to indicate binary states (e.g., true/false or enabled/disabled).
 *
 * @property allowEmpty Determines whether empty values are allowed.
 * @property allowTruthy Allows non-boolean truthy values to be treated as `true`.
 * @property tickElement Defines the element to be displayed for a tick (true) value.
 * @property tickElementAsString Specifies the tick element as an HTML string representation.
 * @property tickElementAsBoolean Specifies the tick element as a boolean representation.
 * @property crossElement Defines the element to be displayed for a cross (false) value.
 * @property crossElementAsString Specifies the cross element as an HTML string representation.
 * @property crossElementAsBoolean Specifies the cross element as a boolean representation.
 */
external interface CellTickCrossFormatterParams {
    var allowEmpty: Boolean?
    var allowTruthy: Boolean?
    var tickElement: dynamic
    var tickElementAsString: String?
    var tickElementAsBoolean: Boolean?
    var crossElement: dynamic
    var crossElementAsString: String?
    var crossElementAsBoolean: Boolean?
}

/**
 * Configures and returns a `CellTickCrossFormatterParams` object.
 *
 * This function allows customization of the properties for a tick/cross formatter,
 * which is typically used to indicate binary states in table columns (e.g., true/false).
 *
 * @param block A lambda function to configure the `CellTickCrossFormatterParams` object.
 * This lambda can define properties such as `allowEmpty`, `allowTruthy`, `tickElement`, and `crossElement`.
 * @return A `CellTickCrossFormatterParams` object configured with the specified properties.
 */
@Suppress("unused")
fun cellTickCrossFormatterParams(block: CellTickCrossFormatterParams.() -> Unit): CellTickCrossFormatterParams {
    val formatterParams: CellTickCrossFormatterParams = unsafeJso(block)
    formatterParams.tickElementAsString?.let { formatterParams.tickElement = it }
    formatterParams.tickElementAsBoolean?.let { formatterParams.tickElement = it }
    formatterParams.crossElementAsString?.let { formatterParams.crossElement = it }
    formatterParams.crossElementAsBoolean?.let { formatterParams.crossElement = it }
    return formatterParams
}
