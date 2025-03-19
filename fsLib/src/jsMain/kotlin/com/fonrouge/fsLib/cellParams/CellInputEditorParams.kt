package com.fonrouge.fsLib.cellParams

import js.objects.jso

/**
 * Encapsulates configuration parameters for an input editor in a column definition.
 *
 * This class is used to define the customizable behavior and properties of an input editor,
 * including whether search functionality is enabled, input masking, content selection, and
 * additional attributes for the input elements.
 *
 * @property search Indicates whether search functionality is enabled for the editor.
 * @property mask Defines an input mask to control the format of the entered data.
 * @property selectContents Specifies whether the editor should select all contents upon activation.
 * @property elementAttributes A map of key-value pairs representing attributes to customize the input element.
 */
external interface CellInputEditorParams {
    var search: Boolean?
    var mask: String?
    var selectContents: Boolean?
    var elementAttributes: Map<String, String>?
}

/**
 * Defines configuration parameters for an input editor in a column definition.
 *
 * This function provides a way to configure the properties of the `ColDefInputEditorParams`
 * class, which allows customization of behaviors such as enabling search functionality,
 * defining input masks, content selection, and setting additional attributes for the input elements.
 *
 * @param block A lambda function used to configure the properties of the `ColDefInputEditorParams` object.
 * The lambda can define properties such as `search` (to enable search functionality),
 * `mask` (to specify an input mask), `selectContents` (to determine whether the input
 * content is selected on activation), and `elementAttributes` (to add custom attributes).
 * @return A `dynamic` object representing the serialized JSON configuration for the input editor parameters.
 */
@Suppress("unused")
fun cellInputEditorParams(block: CellInputEditorParams.() -> Unit): CellInputEditorParams = jso(block)
