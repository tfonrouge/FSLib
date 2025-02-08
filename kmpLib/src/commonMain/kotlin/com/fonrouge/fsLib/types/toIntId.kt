package com.fonrouge.fsLib.types

/**
 * Converts the nullable `Int` instance to an `IntId` of the specified type `T`.
 *
 * This function allows a nullable `Int` to be transformed into a strongly-typed
 * `IntId` associated with type `T`. If the `Int` is `null`, the function will return `null`.
 *
 * @return An `IntId<T>` if the `Int` is non-null, otherwise `null`.
 */
@Suppress("unused")
fun <T : Any> Int?.toIntId(): IntId<T>? = this?.let { IntId(it) }