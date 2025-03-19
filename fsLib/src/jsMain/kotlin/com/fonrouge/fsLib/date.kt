package com.fonrouge.fsLib

import com.fonrouge.fsLib.types.OId
import kotlin.js.Date

/**
 * Extension property to extract the creation date from an `OId` instance.
 *
 * Converts the first 8 characters of the `id` property (assumed to be in hexadecimal
 * format) into a timestamp, representing the number of seconds since the Unix epoch.
 * This timestamp is then multiplied by 1000 to convert it to milliseconds, and
 * subsequently used to create a `Date` object.
 *
 * @receiver The nullable `OId` instance from which the date will be extracted.
 * @return The `Date` object representing the creation time of the `OId`, or `null` if the `OId` is `null`.
 */
val OId<out Any>?.date: Date?
    get() {
        @Suppress("UNUSED_VARIABLE") val hex = this?.id?.substring(0..7)
        val i = js("parseInt(hex, 16) * 1000") as? Int
        return i?.let { Date(i) }
    }
