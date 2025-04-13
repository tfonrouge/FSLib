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
fun EnumEntries<*>.encode(xEnum: XEnum?): String? {
    val a = firstOrNull {
        if (it is XEnum) {
            it.encoded == xEnum?.encoded
        } else false
    }
    return if (a is XEnum) a.encoded else null
}

/**
 * Decodes the given string value into an enum entry of type `T` that implements both `Enum` and `XEnum`.
 *
 * This method iterates through the entries of the enum and checks if the `encoded` value of each entry matches the provided string value.
 * If a match is found, the corresponding enum entry is returned. If no match is found, `null` is returned.
 *
 * @param value The string value to be matched with the `encoded` property of the enum entries. Can be `null`.
 * @return The matching enum entry of type `T` if found, or `null` if no match exists.
 */
fun <T> EnumEntries<T>.find(value: String?): T? where T : Enum<T>, T : XEnum {
    val a: T? = firstOrNull {
        it.encoded == value
    }
    return a
}

/* TODO: find out why this doesn't work:   <Enum<XEnum>>.find("value") -> enum entry
inline fun <reified T> T.find(value: String?): T? where T : XEnum, T : Enum<T> {
    return enumEntries<T>().find(value)
}
*/

/**
 * Retrieves a list of key-value pairs representing the mapping of string identifiers to their corresponding names within an enumeration.
 *
 * For each entry in the enumeration:
 * - If the entry implements the `XEnum` interface, the pair consists of the `encoded` value as the key and the entry's `name` as the value.
 * - If the entry does not implement `XEnum`, both the key and value are set to the entry's `name`.
 *
 * This is useful for creating mappings of enum entries, where additional string encoding is necessary or to provide simple name-to-name mappings for non-`XEnum` entries.
 *
 * @receiver A collection of enumeration entries (`EnumEntries`).
 * @return A list of pairs, where each pair contains the key and value as `String`.
 */
@Suppress("unused")
val EnumEntries<*>.pairs: List<Pair<String, String>>
    get() = map {
        if (it is XEnum) it.encoded to it.name else it.name to it.name
    }
