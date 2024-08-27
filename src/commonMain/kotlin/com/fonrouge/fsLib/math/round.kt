package com.fonrouge.fsLib.math

import kotlin.math.pow

@Suppress("unused")
fun Double.round(decs: Int): Double {
    val mul = 10.0.pow(decs)
    return kotlin.math.round(this.times(mul)).div(mul)
}
