package com.fonrouge.fsLib.types

/**
 * Converts the nullable `String` instance to an `OId` of the specified type `T`.
 *
 * This function allows a nullable `String` to be transformed into a strongly-typed
 * `OId` associated with type `T`. If the `String` is `null`, the function will return `null`.
 *
 * @return An `OId<T>` if the `String` is non-null, otherwise `null`.
 */
@Suppress("unused")
fun <T> String?.toOId(): OId<T>? = this?.let { OId(it) }
