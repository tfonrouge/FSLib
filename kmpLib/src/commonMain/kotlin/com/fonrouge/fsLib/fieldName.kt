package com.fonrouge.fsLib

import kotlin.reflect.KProperty1

/**
 * Concatenates the names of the provided properties into a dot-separated string.
 *
 * This function generates a string representation of the names of the provided Kotlin properties
 * by combining them with a dot (.) as a separator.
 *
 * @param fields A variable number of `KProperty1` objects representing the properties whose names are to be combined.
 * @return A `String` containing the concatenated names of the provided properties, separated by dots.
 */
fun fieldName(vararg fields: KProperty1<*, *>): String = fields.joinToString(".") { it.name }