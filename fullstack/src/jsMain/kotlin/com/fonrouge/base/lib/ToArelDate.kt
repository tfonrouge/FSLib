package com.fonrouge.base.lib

import io.kvision.types.toStringF
import kotlin.js.Date

var defaultDateTimeFormat = "ddd, DD MMM YYYY, HH:mm"
var defaultDateFormat = "ddd, DD MMM YYYY"

/**
 * Extension property for a nullable [Date] object that formats the date and time into a string representation.
 * The formatting is determined by a default date-time format.
 *
 * @receiver The nullable [Date] instance to format.
 * @return A [String] representation of the date and time if the [Date] is not null, or null if the [Date] is null.
 */
@Suppress("unused")
val Date?.toDateTimeString: String?
    get() {
        return this?.toStringF(defaultDateTimeFormat)
    }

/**
 * Converts the nullable Date instance into a formatted String representation using the default date format.
 * If the Date is null, returns null.
 *
 * This property provides a convenient way to transform a Date object into its string format
 * while handling nullability seamlessly.
 */
@Suppress("unused")
val Date?.toDateString: String?
    get() {
        return this?.toStringF(defaultDateFormat)
    }
