package com.fonrouge.fullStack.cellParams

import js.objects.unsafeJso

/**
 * Represents configuration parameters for a number editor in a table cell.
 *
 * This interface is utilized in column definitions to customize the behavior of a numeric cell editor,
 * allowing for control over value boundaries, step increments, input formatting, content selection,
 * and navigation behavior.
 *
 * @property min Specifies the minimum value that the numeric editor can accept.
 * @property max Specifies the maximum value that the numeric editor can accept.
 * @property step Defines the incremental value by which the numeric editor increases or decreases.
 * @property elementAttributes Allows specifying additional dynamic attributes for the editor elements.
 * @property mask Specifies a string-based mask to control the input format.
 * @property selectContents Indicates whether the editor should select all content when activated.
 * @property verticalNavigation Determines the navigation behavior for vertical direction, either to the editor or table.
 */
external interface CellNumberEditorParams {
    var min: Number?
    var max: Number?
    var step: Number?
    var elementAttributes: dynamic
    var mask: String?
    var selectContents: Boolean?
    var verticalNavigation: VerticalNavigationNumber?
}

@Suppress("unused")
enum class VerticalNavigationNumber {
    editor, table
}

/**
 * Configures and returns the parameters for a number editor in a column definition.
 *
 * This function creates a `CellNumberEditorParams` object, which represents the configuration
 * needed for a numeric editor in a table column. The properties of this object, such as minimum
 * or maximum values, input masks, and navigation behavior, can be customized using the provided
 * lambda function.
 *
 * @param block A lambda function used to configure the properties of the `CellNumberEditorParams` object.
 * The lambda can define properties such as `min` (minimum value), `max` (maximum value), `step` (increment step size),
 * `elementAttributes` (custom attributes for the input element), `mask` (for input masking),
 * `selectContents` (to select contents upon activation), and `verticalNavigation` (to specify vertical navigation behavior).
 * @return A `CellNumberEditorParams` object containing the customized configuration for the number editor.
 */
@Suppress("unused")
fun cellNumberEditorParams(block: CellNumberEditorParams.() -> Unit): CellNumberEditorParams = unsafeJso(block)
