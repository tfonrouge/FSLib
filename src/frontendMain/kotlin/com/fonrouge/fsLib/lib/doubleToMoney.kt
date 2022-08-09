package com.fonrouge.fsLib.lib

private val intl = js(
    """
    Intl.NumberFormat('es-MX', {
        style: "currency",
        currency: "MXN"
    })
"""
)

@Suppress("unused")
fun doubleToMoney(double: Double?): String {
    return intl.format(double) as String
}
