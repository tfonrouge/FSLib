package com.fonrouge.fsLib.columnDefinition

import io.kvision.tabulator.js.Tabulator
import js.objects.jso
import kotlin.js.Json

external interface CellListEditorParams {
    var values: Array<Any>?
    var valuesUrl: String?
    var valuesLookup: Any?
    var valuesLookupAsBoolean: Boolean?
    var valuesLookupAsEnumValue: RowRangeLookupValue?
    var valuesLookupAsFunction: (cell: Tabulator.CellComponent, filterTerm: Any) -> Array<Any>?
    var valuesLookupField: String?
    var clearable: Boolean?
    var itemFormatter: ((label: String, value: Any, item: Any, element: Any) -> String)?
    var elementAttributes: Json?
    var verticalNavigation: VerticalNavigation?
    var sort: Sort?
    var defaultValue: String?
    var emptyValue: String?
    var maxWidth: Any?
    var maxWidthAsInt: Int?
    var maxWidthAsBoolean: Boolean?
    var placeholderLoading: Any?
    var placeholderLoadingAsFunction: ((cell: Tabulator.CellComponent, list: Any) -> Any)?
    var placeholderEmpty: Any?
    var placeholderEmptyAsFunction: ((cell: Tabulator.CellComponent, list: Any) -> Any)?
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

enum class RowRangeLookupValue {
    visible, active, selected, range, all
}

enum class VerticalNavigation {
    editor, table, hybrid
}

enum class Sort {
    asc, desc
}

fun cellListEditorParams(block: CellListEditorParams.() -> Unit): CellListEditorParams =
    jso(block)
