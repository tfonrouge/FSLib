package com.fonrouge.fsLib

import com.fonrouge.fsLib.types.OId

/**
 * Generates an `OId` object with a default identifier value of 24 zeroes.
 *
 * This method is useful for creating a placeholder or "empty" object identifier
 * when no specific identifier value is provided.
 *
 * @return An instance of `OId<T>` initialized with a string of 24 zeroes.
 */
@Suppress("unused")
fun <T> emptyOId(): OId<T> = OId("0".repeat(24))
