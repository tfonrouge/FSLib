package com.fonrouge.fsLib.enums

import kotlin.enums.EnumEntries

/**
 * Interface representing an enumerated type with an encoded string representation.
 *
 * This interface is intended to be implemented by enums or other entities that require
 * an encoded string representation, facilitating usage in various utility operations
 * or data serialization and deserialization processes.
 *
 * @property encoded A string representation of the enumeration value, typically used for
 * encoding/decoding or mapping purposes.
 */
interface XEnum {
    val encoded: String
}

/**
 * Retrieves the encoded string representation associated with the given `XEnum` instance
 * from the `EnumEntries`. If no matching encoded value is found, returns `null`.
 *
 * This function iterates through the entries of the enum, comparing the `encoded`
 * property of each entry with the `encoded` property of the given `XEnum` instance.
 *
 * @param xEnum The `XEnum` instance for which the encoded value will be searched. Can be `null`.
 * @return The encoded string representation if a matching entry is found, or `null` if no match exists.
 */
@Suppress("unused")
fun EnumEntries<*>.getEncoded(xEnum: XEnum?): String? {
    val a = firstOrNull {
        if (it is XEnum) {
            it.encoded == xEnum?.encoded
        } else false
    }
    return if (a is XEnum) a.encoded else null
}

/**
 * Generates a list of pairs from the enum entries, where each pair consists of a string
 * representation of the entry. If the entry implements the `XEnum` interface, the pair
 * will contain the `encoded` value as the first element and the entry name as the second element.
 * For non-`XEnum` entries, both elements of the pair will contain the entry name.
 *
 * @return A list of pairs representing the encoded and name values of the enum entries.
 */
@Suppress("unused")
fun EnumEntries<*>.encodedList(): List<Pair<String, String>> {
    return map {
        if (it is XEnum) it.encoded to it.name else it.name to it.name
    }
}
