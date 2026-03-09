package com.fonrouge.fullStack.lib

import io.kvision.utils.numberFormat

/**
 * Formats a nullable number to a string representation with the specified number of decimal places.
 *
 * @param decimals The number of decimal places that the number should be formatted to.
 * @return The formatted number as a string, or an empty string if the number is null.
 */
fun Number?.format(decimals: Int): String {
    return this?.let {
        numberFormat {
            minimumFractionDigits = decimals
            maximumFractionDigits = decimals
        }.format(this)
    } ?: ""
}
