package com.fonrouge.base.math

import kotlin.math.pow

/**
 * Rounds the current Double value to the specified number of decimal places.
 *
 * @param decs The number of decimal places to which the value should be rounded.
 * @return The rounded Double value.
 */
@Suppress("unused")
fun Double.round(decs: Int): Double {
    val mul = 10.0.pow(decs)
    return kotlin.math.round(this.times(mul)).div(mul)
}
