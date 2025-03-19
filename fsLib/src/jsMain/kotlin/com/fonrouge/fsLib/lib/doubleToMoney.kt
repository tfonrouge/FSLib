package com.fonrouge.fsLib.lib

private val intl = js(
    """
    Intl.NumberFormat('es-MX', {
        style: "currency",
        currency: "MXN"
    })
"""
)

/**
 * Converts a nullable [Double] value to a formatted currency string.
 *
 * @param double The nullable [Double] value to be converted.
 * @return A string representing the formatted currency equivalent of the input [Double], or an empty string if the input is null.
 */
@Suppress("unused")
fun doubleToMoney(double: Double?): String {
    return intl.format(double) as String
}
