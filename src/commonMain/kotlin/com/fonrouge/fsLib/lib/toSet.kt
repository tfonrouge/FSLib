package com.fonrouge.fsLib.lib

import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.serializers.IntId
import com.fonrouge.fsLib.serializers.LongId
import com.fonrouge.fsLib.serializers.OId
import com.fonrouge.fsLib.serializers.StringId

@Suppress("unused")
fun <T : BaseDoc<*>> String.toStringIdSet(): Set<StringId<T>> =
    split(',').map { StringId<T>(it) }.toSet()

@Suppress("unused")
fun <T : BaseDoc<*>> String.toIntIdSet(): Set<IntId<T>> =
    split(',').map { IntId<T>(it.toInt()) }.toSet()

@Suppress("unused")
fun <T : BaseDoc<*>> String.toLongIdSet(): Set<LongId<T>> =
    this.split(',').map { LongId<T>(it.toLong()) }.toSet()

@Suppress("unused")
fun <T : BaseDoc<*>> String.toOIdSet(): Set<OId<T>> = this.split(',').map { OId<T>(it) }.toSet()

/**
 * Returns a set containing the current `StringId` instance, or null if the instance is null.
 *
 * If the current `StringId` instance is not null, it will be wrapped in a set and returned. Otherwise, if the instance is null, null will be returned.
 *
 * @param T the type of the `BaseDoc` elements.
 * @return a Set of `StringId` elements, or null if the current instance is null.
 */
@Suppress("unused")
fun <T : BaseDoc<*>> StringId<T>?.setOf(): Set<StringId<T>>? {
    return this?.let { setOf(it) }
}

/**
 * Returns a set containing the specified [IntId] element if it is not null,
 * or an empty set if the element is null.
 *
 * @return a set containing the specified [IntId] element, or an empty set
 *         if the element is null
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


