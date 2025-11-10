package com.fonrouge.fullStack.cellParams

import io.kvision.tabulator.js.Tabulator
import web.mouse.MouseEvent

/**
 * Returns the provided boolean value. This function serves as a utility to handle boolean-based tooltip processing.
 *
 * @param value A boolean value representing the tooltip behavior or state.
 * @return The same boolean value passed as the parameter.
 */
@Suppress("unused")
fun cellTooltipAsBoolean(value: Boolean): Boolean = value

/**
 * Converts the provided value into a tooltip string for a cell.
 *
 * @param value A string representing the value to be used for the cell tooltip.
 * @return A string representation of the cell tooltip, which matches the input value.
 */
@Suppress("unused")
fun cellTooltipAsString(value: String): String = value

/**
 * Generates a tooltip function for a Tabulator cell. The function can be used to create dynamic, custom tooltips
 * based on the cell and mouse event information.
 *
 * @param init A lambda function that is called to initialize the tooltip content. It receives the mouse event,
 *             the cell component, and a callback function to notify when the rendering is complete. The lambda should
 *             return the content of the tooltip or null.
 * @return A lambda function that can be used as a tooltip generator. It takes the mouse event, the cell component,
 *         and the render completion callback as parameters and returns the tooltip content or null.
 *
 *             var el = document.createElement("div");
 *             el.style.backgroundColor = "red";
 *             el.innerText = cell.getColumn().getField() + " - " + cell.getValue(); //return cells "field - value";
 *
 *             return el
 *
 */
@Suppress("unused")
fun cellTooltipAsFun(
    init: (event: MouseEvent, cell: Tabulator.CellComponent, onRendered: () -> Unit) -> Any?,
): (MouseEvent, Tabulator.CellComponent, () -> Unit) -> Any? =
    { e: MouseEvent, cell: Tabulator.CellComponent, onRendered: () -> Unit ->
        init(e, cell, onRendered)
    }
