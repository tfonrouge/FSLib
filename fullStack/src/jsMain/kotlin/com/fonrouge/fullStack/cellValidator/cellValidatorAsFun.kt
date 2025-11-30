package com.fonrouge.fullStack.cellValidator

import io.kvision.tabulator.js.Tabulator

/**
 * Wraps a given cell validation logic into a function compatible with the Tabulator cell validation function structure.
 *
 * @param init A lambda function that implements the cell validation logic. It takes a `Tabulator.CellComponent` object,
 * a dynamic value, and additional dynamic parameters as input and returns a Boolean indicating whether the value is valid.
 * @return A function that conforms to the Tabulator cell validation function signature, executing the provided
 * validation logic.
 */
fun cellValidatorAsFun(
    init: (cell: Tabulator.CellComponent, value: dynamic, parameters: dynamic) -> Boolean,
): (cell: Tabulator.CellComponent, value: dynamic, parameters: dynamic) -> Boolean =
    { cell: Tabulator.CellComponent, value: dynamic, parameters: dynamic ->
        init(cell, value, parameters)
    }
