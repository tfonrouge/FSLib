package com.fonrouge.fsLib.cellParams

import io.kvision.tabulator.js.Tabulator
import js.objects.unsafeJso
import kotlin.js.Json
import kotlin.js.json

/**
 * Represents parameters used for a cell lookup formatter function.
 *
 * This interface is designed to encapsulate a key-value pair structure,
 * which can be utilized within formatter functions for processing
 * or transforming cell data in a specific context.
 *
 * @property key The unique identifier or key associated with the value.
 * @property value The data or content tied to the key, which can be of any type or null.
 */
external interface CellLookupFormatterParamsAsFun {
    var key: String
    var value: Any?
}

/**
 * Converts a block of code that modifies `CellLookupFormatterParamsAsFun` parameters
 * into a function that accepts a `Tabulator.CellComponent` and returns a `Json` result.
 *
 * @param block A lambda function that operates on a `CellLookupFormatterParamsAsFun` instance
 *              and a `Tabulator.CellComponent` to define key-value parameters.
 * @return A function that takes a `Tabulator.CellComponent` and outputs a `Json` object
 *         constructed from the modified `CellLookupFormatterParamsAsFun`.
 */
@Suppress("unused")
fun cellLookupFormatterParamsAsFun(
    block: CellLookupFormatterParamsAsFun.(cell: Tabulator.CellComponent) -> Unit,
): (Tabulator.CellComponent) -> Json {
    return {
        val result = unsafeJso<CellLookupFormatterParamsAsFun>()
        block(result, it)
        json(result.key to result.value)
    }
}
