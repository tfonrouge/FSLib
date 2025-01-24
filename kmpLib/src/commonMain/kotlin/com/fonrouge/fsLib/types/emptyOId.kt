package com.fonrouge.fsLib.types

/**
 * Creates an empty `OId` instance with a default 24-character string of zeros as the identifier.
 *
 * This function is useful for initializing a placeholder or "empty" object identifier
 * when no specific value is available.
 *
 * @return An `OId` object with an identifier consisting of 24 zero characters.
 */
@Suppress("unused")
fun <T> emptyOId(): OId<T> = OId("0".repeat(24))
