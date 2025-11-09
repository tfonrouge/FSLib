package com.fonrouge.fullStack.tabulator

import io.kvision.tabulator.js.Tabulator
import js.objects.Object
import kotlin.reflect.KProperty1

/**
 * Retrieves a value from the data object associated with the cell, following a specified path of properties.
 *
 * @param path A variable-length list of property references indicating the path to the desired value within the data object.
 * @return The value located at the end of the specified path, or null if the path does not exist or the value is undefined.
 */
fun <R> Tabulator.CellComponent.getDataValue(vararg path: KProperty1<*, *>): R? {
    var d: Object = getData() as Object
    path.forEach { k ->
        if (!d.hasOwnProperty(k.name)) return null.asDynamic() as R?
        val x = d.asDynamic()[k.name]
        if (x == null || jsTypeOf(x) === "undefined") return null.asDynamic() as R?
        if (x is Object) {
            d = x
        } else return x as R
    }
    return (d.asDynamic()) as R
}

/**
 * Retrieves the data value associated with the property name from the given Tabulator cell.
 *
 * @param R The type of the value associated with the property.
 * @param cell The Tabulator cell component from which the data value will be retrieved.
 * @return The value associated with the property name from the cell's data, or null if the value is not present or undefined.
 */
@Suppress("unused")
fun <R> KProperty1<*, R?>.getDataValue(cell: Tabulator.CellComponent): R? {
    val d: Object = cell.getData() as Object
    if (!d.hasOwnProperty(name)) return null.asDynamic() as R?
    val x = d.asDynamic()[name]
    if (x == null || jsTypeOf(x) === "undefined") return null.asDynamic() as R?
    return x as R
}
