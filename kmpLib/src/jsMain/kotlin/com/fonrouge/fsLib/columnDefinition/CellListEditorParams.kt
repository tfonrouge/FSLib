@file:Suppress("unused", "EnumEntryName")

package com.fonrouge.fsLib.columnDefinition

import io.kvision.tabulator.js.Tabulator
import js.objects.jso
import kotlin.js.Json
import kotlin.js.Promise

/**
 * Represents the configuration parameters for a cell editor using a list lookup mechanism.
 *
 * This interface provides an extensive set of properties to configure the behavior and
 * appearance of editors in a tabulator table, including customization of values, lookup functionality,
 * sorting, filtering, and various formatting options for the editor.
 *
 * @property values The source of values for the editor, which can be dynamic or static.
 * @property valuesAsArray The array of values provided as options for the editor.
 * @property valuesAsObject A JSON object representing the set of values for the editor.
 * @property valuesAsValueLabelArray An array of `ValueObject` instances, each containing properties for value and label.
 * @property valuesUrl A URL source used to fetch the values for the editor.
 * @property valuesLookup Represents dynamic lookup configuration, which can be set to various types for customization.
 * @property valuesLookupAsBoolean A boolean indicating whether to utilize a lookup-based configuration.
 * @property valuesLookupAsEnumValue Specifies a pre-defined lookup behavior that aligns with `RowRangeLookupValue` enumeration.
 * @property valuesLookupAsFunArray A dynamic callback function returning an array of values for the editor, based on the provided cell context and filter term.
 * @property valuesLookupAsFunObject A dynamic callback function returning a JSON object with lookup values for the editor.
 * @property valuesLookupField Specifies the field key used for determining lookup values.
 * @property clearable Boolean flag to indicate whether the editor can clear its current value.
 * @property itemFormatter Function for custom formatting of items in the editor dropdown or list.
 * @property elementAttributes JSON object for specifying additional dynamic attributes for the editor's DOM elements.
 * @property verticalNavigation Configures navigation behavior in vertical direction using `VerticalNavigation` enum.
 * @property sort Configures sorting of the editor's options, which can be set to various types for flexible sorting criteria.
 * @property sortAsEnum Specifies sorting order as `Sort` enumeration.
 * @property sortAsFun Callback function for defining custom sorting logic between labels and values.
 * @property defaultValue Default value set in the editor before interaction.
 * @property emptyValue Value used when the editor is cleared or no value is selected.
 * @property maxWidth Maximum width of the editor; can be dynamic or an integer.
 * @property maxWidthAsInt Configures the maximum width of the editor in pixels as an integer.
 * @property maxWidthAsBoolean Configures the maximum width of the editor with a boolean flag.
 * @property placeholderLoading Placeholder text or configuration shown while options are being loaded.
 * @property placeholderLoadingAsFun Callback function for dynamically generating a loading placeholder based on the cell context and list.
 * @property placeholderEmpty Placeholder text or configuration displayed when no options are available.
 * @property placeholderEmptyAsFun Callback function for dynamically generating an empty-value placeholder based on the cell context and list.
 * @property multiselect Enables multi-selection mode for the editor.
 * @property autocomplete Configures whether an autocomplete or search mechanism is enabled for the editor.
 * @property filterFunc Callback function for defining the filtering logic applied to lookup options based on the input term.
 * @property filterRemote Boolean flag indicating whether filtering relies on remote or server-side logic.
 * @property filterDelay Time delay in milliseconds before applying the filter after user input.
 * @property allowEmpty Boolean flag to specify if empty values are allowed in the editor.
 * @property listOnEmpty Indicates if the dropdown list should be displayed when there is no input value.
 * @property mask Defines an input mask for controlling valid input formats in the editor.
 * @property freetext Enables free-text input mode, allowing users to enter values not limited to predefined options.
 */
external interface CellListEditorParams {
    var values: dynamic
    var valuesAsArray: Array<Any>?
    var valuesAsObject: Json?
    var valuesAsValueLabelArray: Array<ValueLabel>?
    var valuesUrl: String?
    var valuesLookup: Any?
    var valuesLookupAsBoolean: Boolean?
    var valuesLookupAsEnumValue: RowRangeLookupValue?
    var valuesLookupAsFunArray: ((cell: Tabulator.CellComponent, filterTerm: Any) -> Array<Any>)?
    var valuesLookupAsFunObject: ((cell: Tabulator.CellComponent, filterTerm: Any) -> Json)?
    var valuesLookupAsPromise: ((cell: Tabulator.CellComponent, filterTerm: Any) -> Promise<Any>)?
    var valuesLookupField: String?
    var clearable: Boolean?
    var itemFormatter: ((label: String, value: Any, item: Any, element: Any) -> String)?
    var elementAttributes: Json?
    var verticalNavigation: VerticalNavigation?
    var sort: Any?
    var sortAsEnum: Sort?
    var sortAsFun: ((aLabel: String, bLabel: String, aValue: Any, bValue: Any, aItem: Any, bItem: Any) -> Number)?
    var defaultValue: String?
    var emptyValue: String?
    var maxWidth: Any?
    var maxWidthAsInt: Int?
    var maxWidthAsBoolean: Boolean?
    var placeholderLoading: Any?
    var placeholderLoadingAsFun: ((cell: Tabulator.CellComponent, list: Any) -> Any)?
    var placeholderEmpty: Any?
    var placeholderEmptyAsFun: ((cell: Tabulator.CellComponent, list: Any) -> Any)?
    var multiselect: Boolean?
    var autocomplete: Boolean?
    var filterFunc: ((term: String, label: String, value: Any, item: Any) -> Boolean)?
    var filterRemote: Boolean?
    var filterDelay: Int?
    var allowEmpty: Boolean?
    var listOnEmpty: Boolean?
    var mask: String?
    var freetext: Boolean?
}

/**
 * Represents a value object commonly used in select or hierarchical structures.
 *
 * This interface defines the structure of an object that encapsulates a value-label pair
 * with additional optional properties for nested options and attribute customization.
 *
 * @property value The value represented by this object, typically used as the option's identifier.
 * @property label The human-readable label associated with the value, often displayed in the UI.
 * @property options An array of nested `ValueObject` instances, representing hierarchical or grouped options.
 * @property elementAttributes A dynamic JSON object containing custom attributes for the associated UI element.
 */
external interface ValueLabel {
    var value: String?
    var label: String?
    var options: Array<ValueLabel>?
    var elementAttributes: Json?
}

/**
 * Creates and returns a `ValueObject` instance by applying a given configuration block.
 *
 * This function is used to generate a `ValueObject` through a lambda configuration block,
 * which allows customization of properties such as `value`, `label`, `options`, and
 * `elementAttributes` for defining hierarchical or selectable structures.
 *
 * @param block A lambda function used to configure the properties of the `ValueObject`. The block
 * provides access to modify properties directly, such as `value` (the identifier), `label`
 * (a human-readable label), `options` (nested `ValueObject` instances), and
 * `elementAttributes` (custom attributes for the UI element).
 * @return A configured `ValueObject` instance with the applied properties.
 */
fun valueLabel(block: ValueLabel.() -> Unit): ValueLabel = jso(block)

/**
 * Represents the possible range lookup values for rows in a table or data grid.
 *
 * This enum defines various categories that can be used to specify the scope or subset of rows
 * affected or considered in an operation. The values include:
 *
 * - `visible`: Rows currently visible in the table viewport
 * - `active`: Rows currently in the table (rows that pass current filters etc)
 * - `selected`: Rows currently selected rows by the selection module (this includes not currently active rows)
 * - `range`: Any currently selected ranges from the range selection module
 * - `all`: All rows in the table regardless of filters
 */
enum class RowRangeLookupValue {
    visible, active, selected, range, all
}

/**
 * Represents the navigation behavior options for vertical movement within certain UI components.
 *
 * This enum class defines the available navigation modes for handling vertical interactions,
 * which can be used to determine whether focus and navigation should occur within an editor,
 * a table, or a hybrid approach combining both behaviors.
 *
 * Available options include:
 * - `editor`: Limits navigation to within the editor.
 * - `table`: Focuses on table-wide navigation and interactions.
 * - `hybrid`: Combines behaviors of both editor and table navigation.
 */
enum class VerticalNavigation {
    editor, table, hybrid
}

/**
 * Represents the sorting order for a field or dataset.
 *
 * This enum is used to specify the direction of sorting, either in ascending or descending order.
 * It can be applied in various contexts such as column definitions in tables or query operations
 * to arrange data based on the specified order.
 */
enum class Sort {
    asc, desc
}

/**
 * Configures and initializes the parameters for a cell list editor using a specified block of code.
 * Allows customization of various aspects of the editor, such as value sources, sorting, filtering,
 * placeholders, and other behaviors within a tabulator table setting.
 *
 * @param block A lambda function enabling configuration of the `CellListEditorParams` instance.
 * It provides access to various properties for the editor's customization.
 *
 * @return Returns a configured instance of `CellListEditorParams` after applying customizations
 * specified in the provided block.
 */
fun cellListEditorParams(block: CellListEditorParams.() -> Unit): CellListEditorParams {
    val result = jso(block)
    result.valuesAsArray?.let { result.values = it }
    result.valuesAsObject?.let { result.values = it }
    result.valuesAsValueLabelArray?.let { result.values = it }
    result.valuesLookupAsBoolean?.let { result.valuesLookup = it }
    result.valuesLookupAsEnumValue?.let { result.valuesLookup = it }
    result.valuesLookupAsFunArray?.let { result.valuesLookup = it }
    result.valuesLookupAsFunObject?.let { result.valuesLookup = it }
    result.valuesLookupAsPromise?.let { result.valuesLookup = it }
    result.sortAsEnum?.let { result.sort = it.name }
    result.sortAsFun?.let { result.sort = it }
    result.maxWidthAsInt?.let { result.maxWidth = it }
    result.maxWidthAsBoolean?.let { result.maxWidth = it }
    result.placeholderLoadingAsFun?.let { result.placeholderLoading = it }
    result.placeholderEmptyAsFun?.let { result.placeholderEmpty = it }
    return result
}
