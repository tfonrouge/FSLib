package com.fonrouge.fsLib.regex

import org.bson.conversions.Bson
import org.litote.kmongo.regex
import kotlin.reflect.KProperty

@Suppress("unused")
fun String.regexAcute(spaceAsStar: Boolean = true): String {
    var result = ""
    forEach {
        result += when (it) {
            'a' -> "[aá]"
            'e' -> "[eé]"
            'i' -> "[ií]"
            'o' -> "[oó]"
            'u' -> "[uú]"
            'n' -> "[nñ]"
            'A' -> "[AÁ]"
            'E' -> "[EÉ]"
            'I' -> "[IÍ]"
            'O' -> "[OÓ]"
            'U' -> "[UÚ]"
            'N' -> "[NÑ]"
            else -> it
        }
    }
    return if (spaceAsStar) result.replace(" ", " .*") else result
}

@Suppress("unused")
fun KProperty<String?>.regexAcute(search: String, spaceAsStar: Boolean = true, ignoreCase: Boolean = true): Bson =
    if (ignoreCase)
        regex(Regex(search.regexAcute(spaceAsStar = spaceAsStar), option = RegexOption.IGNORE_CASE))
    else
        regex(search.regexAcute(spaceAsStar = spaceAsStar))
