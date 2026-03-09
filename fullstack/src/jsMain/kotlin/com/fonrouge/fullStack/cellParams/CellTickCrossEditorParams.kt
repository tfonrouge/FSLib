package com.fonrouge.fullStack.cellParams

import js.objects.unsafeJso

/**
 * Represents the parameters for a tick/cross cell editor in a Tabulator table.
 *
 * This interface defines configurable options for a cell editor that allows toggling
 * between a tick (true), cross (false), or an optional indeterminate state.
 *
 * @property trueValue The value to be set when the editor is toggled to the "true" (tick) state.
 * @property falseValue The value to be set when the editor is toggled to the "false" (cross) state.
 * @property tristate An optional flag to enable a tri-state mode. When set to true, the editor supports
 *                    an additional indeterminate state beyond true and false.
 * @property indeterminateValue The value to be assigned when the editor is in the indeterminate state.
 * @property elementAttributes A dynamic object that can specify additional attributes for the editor's
 *                              HTML element, such as custom data attributes or DOM properties.
 */
external interface CellTickCrossEditorParams {
    var trueValue: dynamic
    var falseValue: dynamic
    var tristate: Boolean?
    var indeterminateValue: dynamic
    var elementAttributes: dynamic
}

/**
 * Configures and returns the parameters for a tick/cross cell editor in a Tabulator table.
 *
 * This method allows customization of a tick/cross cell editor by providing a configuration
 * block. The editor supports toggling between tick (true) and cross (false) values, with
 * optional support for an indeterminate state.
 *
 * @param block A configuration block to initialize the properties of the `CellTickCrossEditorParams` object.
 *              The block provides access to modify properties such as trueValue, falseValue, tristate,
 *              indeterminateValue, and elementAttributes.
 * @return A configured `CellTickCrossEditorParams` object ready to be used in a Tabulator table.
 */
@Suppress("unused")
fun cellTickCrossEditorParams(block: CellTickCrossEditorParams.() -> Unit): CellTickCrossEditorParams = unsafeJso(block)
