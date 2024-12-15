package com.fonrouge.fsLib

import com.fonrouge.fsLib.types.OId

/**
 * Generates a new `OId` instance with an ID composed of 24 zero characters.
 *
 * @return A new instance of `OId` with a default ID of 24 zeros.
 */
@Suppress("unused")
fun <T> nullOId(): OId<T> = OId("0".repeat(24))
