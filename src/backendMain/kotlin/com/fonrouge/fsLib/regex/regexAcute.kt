package com.fonrouge.fsLib.regex

@Suppress("unused")
fun String.regexAcute(): String {
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
    return result
}
