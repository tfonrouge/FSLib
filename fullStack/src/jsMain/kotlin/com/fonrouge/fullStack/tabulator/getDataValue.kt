package com.fonrouge.fullStack.tabulator

import com.fonrouge.base.types.*
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

/**
 * Converts a data value retrieved from the cell's data object into an `IntId` of the specified type `T`.
 *
 * This method utilizes a property path to extract a value from the underlying data object
 * of the cell, converts it into an `Int`, and wraps it as an `IntId<T>`. If the data value or
 * the resulting `Int` is null, the method will return null.
 *
 * @param T The type associated with the resulting `IntId`.
 * @param path The property path used to access the value in the data object.
 * @return An `IntId<T>` created from the extracted integer value, or null if the data value is null.
 */
@Suppress("unused")
fun <T> Tabulator.CellComponent.toIntId(vararg path: KProperty1<*, *>): IntId<T>? = getDataValue<Int>(*path)?.toIntId()

/**
 * Converts the data value retrieved from the `Tabulator.CellComponent` using the specified property path
 * into a `LongId` of the specified type `T`.
 *
 * This function first extracts the data value as a nullable `Long` from the `CellComponent` using the provided
 * property path. If the value is non-null, it is then converted into a strongly-typed `LongId<T>`.
 *
 * @param T The generic type associated with the resulting `LongId`.
 * @param path A vararg of `KProperty1` instances representing the property path to navigate within the `CellComponent`'s data object.
 *             These properties are accessed to locate the value to be converted into a `LongId<T>`.
 * @return A `LongId<T>` if the data value exists and is non-null, otherwise returns `null`.
 */
@Suppress("unused")
fun <T> Tabulator.CellComponent.toLongId(vararg path: KProperty1<*, *>): LongId<T>? =
    getDataValue<Long>(*path)?.toLongId()

/**
 * Converts a value retrieved from the `CellComponent`'s data object, at the given property path, to a `StringId` of the specified type `T`.
 *
 * This function allows extracting data from a `CellComponent` using a property path
 * and converts the resulting value, if it's a `String`, into a strongly-typed `StringId<T>`.
 * If the retrieved value is null or not a `String`, the function will return null.
 *
 * @param T The type associated with the resulting `StringId`.
 * @param path A vararg of `KProperty1` representing the path to navigate within the data object for value extraction.
 * @return A `StringId<T>` created from the retrieved string value if non-null, otherwise null.
 */
@Suppress("unused")
fun <T> Tabulator.CellComponent.toStringId(vararg path: KProperty1<*, *>): StringId<T>? =
    getDataValue<String>(*path)?.toStringId()

/**
 * Converts the data value retrieved from a `CellComponent` to an `OId` of the specified type `T`,
 * using the provided property path to locate the value in the data object.
 *
 * @param T The type associated with the `OId`.
 * @param path A vararg of `KProperty1` representing the property path used to extract the data value
 *             from the `CellComponent`'s data object.
 * @return An `OId<T>` representation of the data value if the value exists and can be converted; otherwise, `null`.
 */
@Suppress("unused")
fun <T> Tabulator.CellComponent.toOId(vararg path: KProperty1<*, *>): OId<T>? = getDataValue<String>(*path)?.toOId()
