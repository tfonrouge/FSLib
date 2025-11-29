package com.fonrouge.fullStack.tabulator

import io.kvision.tabulator.js.Tabulator
import js.objects.Object
import kotlin.reflect.KProperty1

/**
 * Retrieves a value from a nested object structure based on the provided path of properties.
 *
 * @param R The expected return type of the value.
 * @param path A vararg of property references representing the path to traverse within the object.
 * @return The value of type R if the path exists and is accessible; otherwise, null.
 */
fun <R> Object.getDataValue(vararg path: KProperty1<*, *>): R? {
    var d: Object = this
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
 * Retrieves a data value from the CellComponent's data object based on the specified property path.
 *
 * @param R The type of the value to retrieve.
 * @param path A vararg of KProperty1 representing the path to navigate within the data object to extract the value.
 * @return The value of type R if the path exists and is accessible; otherwise, returns null.
 */
fun <R> Tabulator.CellComponent.getDataValue(vararg path: KProperty1<*, *>): R? =
    (getData() as Object).getDataValue(*path)

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
