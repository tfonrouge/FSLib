package com.fonrouge.fsLib.regex

import org.litote.kmongo.regex
import kotlin.reflect.KProperty

/**
 * Transforms the string to a format where each vowel and the letter 'n'
 * are replaced with a regex pattern that matches both the regular and
 * acute versions of the character. If `spaceAsStar` is true, spaces are
 * replaced with " .*".
 *
 * @param spaceAsStar If true, spaces will be replaced with " .*".
 * @return The transformed string with regex patterns.
 */
fun String.regexAcute(
    spaceAsStar: Boolean = true,
): String {
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

/**
 * Extension function for KProperty<String?> to perform regex matching on a string,
 * transforming it to match both regular and acute versions of specific characters.
 *
 * @param search The string to search for, which will be transformed to a regex pattern
 *        to match both regular and acute versions of specific characters.
 * @param spaceAsStar If true, spaces will be replaced with " .*" in the regex pattern.
 * @param ignoreCase If true, the regex search will be case-insensitive.
 */
@Suppress("unused")
fun KProperty<String?>.regexAcute(
    search: String,
    spaceAsStar: Boolean = true,
    ignoreCase: Boolean = true,
) = if (ignoreCase)
    regex(Regex(search.regexAcute(spaceAsStar = spaceAsStar), option = RegexOption.IGNORE_CASE))
else
    regex(search.regexAcute(spaceAsStar = spaceAsStar))
