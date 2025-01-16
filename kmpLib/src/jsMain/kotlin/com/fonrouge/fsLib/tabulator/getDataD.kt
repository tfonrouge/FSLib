package com.fonrouge.fsLib.tabulator

import io.kvision.tabulator.js.Tabulator
import kotlin.reflect.KProperty1

/**
 * Retrieves nested data from the current cell's data object based on the provided sequence of Kotlin property references.
 *
 * @param path A vararg of Kotlin property references representing the path to the desired data.
 * @return The value at the specified path within the cell's data, cast to the specified generic type [R],
 *         or null if the data does not exist or cannot be cast to [R].
 */
@Suppress("unused")
fun <R> Tabulator.CellComponent.getDataD(vararg path: KProperty1<*, *>): R? {
    var d = getData().asDynamic()
    path.forEach { k ->
        d = d[k.name]
    }
    return d as? R
}
