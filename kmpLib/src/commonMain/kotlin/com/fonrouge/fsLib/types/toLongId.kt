package com.fonrouge.fsLib.types

/**
 * Converts the nullable `Long` instance to a `LongId` of the specified type `T`.
 *
 * This function allows a nullable `Long` to be transformed into a strongly-typed
 * `LongId` associated with type `T`. If the `Long` is `null`, the function will return `null`.
 *
 * @return A `LongId<T>` if the `Long` is non-null, otherwise `null`.
 */
@Suppress("unused")
fun <T : Any> Long?.toLongId(): LongId<T>? = this?.let { LongId(it) }
