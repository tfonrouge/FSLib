package com.fonrouge.fsLib.enums

import kotlin.enums.EnumEntries

interface XEnum {
    val encoded: String
}

@Suppress("unused")
fun EnumEntries<*>.getEncoded(xEnum: XEnum?): String? {
    val a = firstOrNull {
        if (it is XEnum) {
            it.encoded == xEnum?.encoded
        } else false
    }
    return if (a is XEnum) a.encoded else null
}

@Suppress("unused")
fun EnumEntries<*>.encodedList(): List<Pair<String, String>> {
    return map {
        if (it is XEnum) it.encoded to it.name else it.name to it.name
    }
}
