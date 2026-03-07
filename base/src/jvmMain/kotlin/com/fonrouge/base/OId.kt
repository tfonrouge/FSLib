package com.fonrouge.base

import com.fonrouge.base.types.OId
import java.time.OffsetDateTime

/**
 * Generates an `OId` instance based on the provided `OffsetDateTime`.
 *
 * The method converts the `OffsetDateTime` to seconds since the epoch, encodes
 * the value as a hexadecimal string, and pads it to form the initial part of the identifier.
 * The result is then concatenated with a fixed-length padding to create the formatted `OId`.
 *
 * @param T The type associated with the identifier.
 * @param offsetDateTime The `OffsetDateTime` instance used to generate the identifier.
 * @return An `OId<T>` initialized with a hexadecimal string identifier based on the given `OffsetDateTime`.
 */
@Suppress("unused")
fun <T> OId(offsetDateTime: OffsetDateTime): OId<T> =
    OId(offsetDateTime.toEpochSecond().toString(16).padStart(8, '0') + "0000000000000000")
