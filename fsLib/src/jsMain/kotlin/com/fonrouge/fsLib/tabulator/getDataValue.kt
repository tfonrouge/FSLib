package com.fonrouge.fsLib.tabulator

import io.kvision.tabulator.js.Tabulator
import js.objects.Object
import kotlin.reflect.KProperty1

/**
 * Retrieves nested data from the current cell's data object based on the provided sequence of Kotlin property references.
 *
 * @param path A vararg of Kotlin property references representing the path to the desired data.
 * @return The value at the specified path within the cell's data, cast to the specified generic type [R],
 *         or null if the data does not exist or cannot be cast to [R].
 */
fun <R> Tabulator.CellComponent.getDataValue(vararg path: KProperty1<*, *>): R {
    var d: Object = getData() as Object
    path.forEach { k ->
        if (!d.hasOwnProperty(k.name)) return null.asDynamic() as R
        val x = d.asDynamic()[k.name]
        if (x is Object) {
            d = x
        } else return x as R
    }
    return (d.asDynamic()) as R
}
