package com.fonrouge.fsLib.lib

/**
 * Converts a comma-separated string into an encoded string representation wrapped in brackets
 * with each element quoted. A transformation can be applied to the list of split strings.
 *
 * @param transform A lambda function that operates on the list of strings derived from splitting
 * the input string. Defaults to the identity transformation.
 * @return A string representation of the transformed and encoded list, enclosed in brackets,
 * with each element quoted and separated by commas.
 */
@Suppress("unused")
fun String.toEncodedStringList(transform: (List<String>.() -> Collection<String>) = { this }): String =
    this.split(",")
        .transform()
        .joinToString(
            separator = ",",
            prefix = "[",
            postfix = "]",
            transform = { "\"$it\"" }
        )

/**
 * Converts the input string into an encoded list of integers, applies a transformation, and returns
 * a formatted string representation of the transformed list.
 *
 * The input string is expected to contain comma-separated values. Each value is parsed into an integer,
 * and the resulting list of integers is transformed using the provided transformation function.
 * The final result is returned as a string in JSON array-like format with each integer quoted.
 *
 * @param transform A transformation function that operates on the list of integers. By default,
 * it returns the original list without any modifications.
 * @return A string representation of the transformed list in JSON array-like format, where each
 * integer is quoted. If parsing fails for any value, that value is ignored in the output.
 */
@Suppress("unused")
fun String.toEncodedIntList(transform: (List<Int>.() -> Collection<Int>) = { this }): String =
    this.split(",")
        .mapNotNull { it.toIntOrNull() }
        .transform()
        .joinToString(
            separator = ",",
            prefix = "[",
            postfix = "]",
            transform = { "\"$it\"" }
        )
