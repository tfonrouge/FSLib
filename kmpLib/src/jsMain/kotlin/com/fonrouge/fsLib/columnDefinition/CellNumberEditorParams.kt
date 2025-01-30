package com.fonrouge.fsLib.columnDefinition

import js.objects.jso

/**
 * Represents the configuration parameters for a number editor in a column definition.
 *
 * This class is used to specify constraints and behavior for a numeric editor in a table column.
 *
 * @property min The minimum allowed value for the editor.
 * @property max The maximum allowed value for the editor.
 * @property step The step size for incrementing or decrementing the value.
 * @property elementAttributes A map of attributes to set on the input element.
 * @property mask A string representing an input mask for the editor.
 * @property selectContents A flag indicating whether the editor should select the contents upon activation.
 * @property verticalNavigation Specifies the navigation behavior in the vertical direction, using the `VerticalNavigation` enum.
 *
 * Reference: https://tabulator.info/docs/6.3/edit#editor-number
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
 * Defines configuration parameters for a number editor in a column definition.
 *
 * @param block A lambda function used to configure the properties of the ColDefNumberEditorParams object, such as minimum value, maximum value, step size, input mask, and additional
 *  attributes.
 * @return A dynamic object representing the serialized JSON configuration for the number editor parameters.
 */
@Suppress("unused")
fun cellNumberEditorParams(block: CellNumberEditorParams.() -> Unit): CellNumberEditorParams = jso(block)
