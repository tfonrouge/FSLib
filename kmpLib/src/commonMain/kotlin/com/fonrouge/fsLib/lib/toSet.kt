package com.fonrouge.fsLib.lib

import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.types.IntId
import com.fonrouge.fsLib.types.LongId
import com.fonrouge.fsLib.types.OId
import com.fonrouge.fsLib.types.StringId

/**
 * Converts a comma-separated string into a set of `StringId` instances for a specified type.
 *
 * @return A set of `StringId` instances created from the comma-separated values in the input string.
 */
@Suppress("unused")
fun <T : BaseDoc<*>> String.toStringIdSet(): Set<StringId<T>> =
    split(',').map { StringId<T>(it) }.toSet()

/**
 * Converts a comma-separated string of integers into a set of IntId objects.
 *
 * @return A set of IntId objects created from the integers in the string.
 */
@Suppress("unused")
fun <T : BaseDoc<*>> String.toIntIdSet(): Set<IntId<T>> =
    split(',').map { IntId<T>(it.toInt()) }.toSet()

/**
 * Converts a comma-separated string into a set of LongId objects.
 *
 * @return A set of LongId objects created from the comma-separated string.
 */
@Suppress("unused")
fun <T : BaseDoc<*>> String.toLongIdSet(): Set<LongId<T>> =
    this.split(',').map { LongId<T>(it.toLong()) }.toSet()

/**
 * Converts a comma-separated string of object ids into a set of `OId<T>` objects.
 *
 * @return a set of `OId<T>` objects parsed from the string
 */
@Suppress("unused")
fun <T : BaseDoc<*>> String.toOIdSet(): Set<OId<T>> = this.split(',').map { OId<T>(it) }.toSet()

/**
 * Converts a nullable StringId to a singleton set containing the StringId, or null if the input is null.
 *
 * @return A set containing the single StringId element if the input is not null, or null if the input is null.
 */
@Suppress("unused")
fun <T : BaseDoc<*>> StringId<T>?.setOf(): Set<StringId<T>>? {
    return this?.let { setOf(it) }
}

/**
 * Converts a nullable `IntId<T>` into a Set containing the `IntId<T>` if it is non-null.
 *
 * @return A Set containing the `IntId<T>` if it is non-null, otherwise `null`.
 */
@Suppress("unused")
fun <T : BaseDoc<*>> IntId<T>?.setOf(): Set<IntId<T>>? {
    return this?.let { setOf(it) }
}

/**
 * Returns a set containing the receiver [LongId] object.
 *
 * The [setOf] method is an extension function that can be called on nullable [LongId] objects.
 * It creates a set containing the receiver object if it is not null, otherwise it returns null.
 * The type parameter [T] must extend [BaseDoc], ensuring that the [LongId] object is compatible with base document types.
 *
 * @return a set containing the receiver [LongId] object, or null if the receiver object is null.
 */
@Suppress("unused")
fun <T : BaseDoc<*>> LongId<T>?.setOf(): Set<LongId<T>>? {
    return this?.let { setOf(it) }
}

/**
 * Creates a set containing the receiver [OId] object.
 *
 * The [setOf] method is an extension function that can be called on nullable [OId] objects.
 * It creates a set containing the receiver object if it is not null, otherwise it returns null.
 * The type parameter [T] must extend [BaseDoc], ensuring that the [OId] object is compatible with base document types.
 *
 * @return a set containing the receiver [OId] object, or null if the receiver object is null.
 */
@Suppress("unused")
fun <T : BaseDoc<*>> OId<T>?.setOf(): Set<OId<T>>? {
    return this?.let { setOf(it) }
}

