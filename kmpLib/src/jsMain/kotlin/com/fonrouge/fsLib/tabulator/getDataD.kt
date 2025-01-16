package com.fonrouge.fsLib.tabulator

import io.kvision.tabulator.js.Tabulator
import kotlin.reflect.KProperty1

/**
 * Retrieves a nested property value from the dynamic data object associated with the `CellComponent`.
 *
 * @param path A variable number of `KProperty1` objects that specify the path of properties to traverse
 *             in the data object. Each property represents a step in the path.
 * @return The value of the nested property specified by the given path, or `null` if any step in the
 *         path does not exist in the data object.
 */
@Suppress("unused")
fun Tabulator.CellComponent.getDataD(vararg path: KProperty1<*, *>): dynamic {
    var d = getData().asDynamic()
    path.forEach { k ->
        d = d[k.name]
    }
    return d
}
