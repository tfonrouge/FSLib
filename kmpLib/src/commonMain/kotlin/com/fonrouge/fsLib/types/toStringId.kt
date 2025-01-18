package com.fonrouge.fsLib.types

/**
 * Converts the nullable `String` instance to a `StringId` of the specified type `T`.
 *
 * This function allows a nullable `String` to be transformed into a strongly-typed
 * `StringId` associated with type `T`. If the `String` is `null`, the function will return `null`.
 *
 * @return A `StringId<T>` if the `String` is non-null, otherwise `null`.
 */
@Suppress("unused")
fun <T> String?.toStringId(): StringId<T>? = this?.let { StringId<T>(it) }
