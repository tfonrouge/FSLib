package com.fonrouge.base.math

import kotlin.math.abs
import kotlin.math.pow

/**
 * Compares the current Double value with another Double value for equality,
 * allowing a specified number of decimal places for precision.
 *
 * @param value The Double value to compare against.
 * @param decimals The number of decimal places to consider for the comparison. Defaults to 3.
 * @return True if the two values are considered equal within the specified decimal precision, false otherwise.
 */
fun Double.equals(value: Double, decimals: Int = 3): Boolean {
    return abs(this - value) < (1 / 10.0.pow(decimals))
}
