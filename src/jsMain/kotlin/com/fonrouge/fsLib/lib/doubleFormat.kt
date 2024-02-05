package com.fonrouge.fsLib.lib

import io.kvision.utils.numberFormat

@Suppress("unused")
fun Double?.format(decimals: Int): String {
    return this?.let {
        numberFormat {
            minimumFractionDigits = decimals
            maximumFractionDigits = decimals
        }.format(this)
    } ?: ""
}