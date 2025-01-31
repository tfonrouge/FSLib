package com.fonrouge.fsLib.cellEditor

import io.kvision.core.onEvent
import io.kvision.form.InputSize
import io.kvision.form.number.NumericInput
import io.kvision.tabulator.js.Tabulator
import org.w3c.dom.HTMLInputElement
import kotlin.reflect.KProperty1

/**
 * Creates a numeric cell editor for a Tabulator table cell, allowing for numeric input with optional initialization
 * and success handling.
 *
 * @param T The type of the data object used in the table.
 * @param field A property reference to the numeric field in the data object.
 * @param init An optional initialization block that can configure the editor with the initial value.
 * @param onSuccess An optional callback invoked when the editing is successfully completed. Receives the new value and the cell component as parameters.
 * @return A function that handles the cell editor creation. The function takes parameters for the cell component, rendered callback,
 *         success handler, cancel handler, and the table row data. It returns a configured NumericInput instance.
 */
@Suppress("unused")
fun <T> cellEditorNumeric(
    field: KProperty1<T, Number>,
    init: (NumericInput.(Number?) -> Unit)? = null,
    onSuccess: ((Number?, Tabulator.CellComponent) -> Unit)? = null
): (
    cell: Tabulator.CellComponent,
    onRendered: (() -> Unit) -> Unit,
    success: (Number?) -> Unit,
    cancel: (Number?) -> Unit,
    T
) -> NumericInput {
    return { cell, onRendered, success, cancel, data ->
        var successCalled = false
        val funSuccess = fun(n: Number?) {
            if (!successCalled) {
                successCalled = true
                cell.checkHeight()
                success(n)
                onSuccess?.invoke(n, cell)
            }
        }
        val initValue = field.get(data)
        NumericInput(
            value = initValue,
        ) {
            size = InputSize.SMALL
            onRendered {
                (getElementD() as HTMLInputElement).select()
            }
            init?.invoke(this, initValue)
            onEvent {
                blur = {
                    funSuccess(self.value)
                }
                change = {
                    funSuccess(self.value)
                }
                keydown = { e ->
                    when (e.keyCode) {
                        13 -> funSuccess(self.value)
                        27 -> cancel(initValue)
                    }
                }
            }
        }
    }
}
