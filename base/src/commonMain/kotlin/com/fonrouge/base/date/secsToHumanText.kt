package com.fonrouge.base.date

import kotlin.math.abs

/**
 * Converts a duration in seconds into a human-readable text format.
 *
 * @param secs the duration in seconds to be converted
 * @return a human-readable string representation of the duration in days, hours, minutes, and seconds
 */
@Suppress("unused")
fun secsToHumanText(secs: Int): String {
    val isNegative = secs < 0
    var s1 = abs(secs)
    var r = ""
    while (s1 > 0) {
        when (s1) {
            in 0..59 -> {
                r += "${s1}s"
                s1 -= s1
            }

            in 60..3599 -> {
                val m: Int = s1 / 60
                s1 -= m * 60
                r += "${m}m "
            }

            in 3600..86399 -> {
                val h: Int = s1 / 3600
                s1 -= h * 3600
                r += "${h}h "
            }

            in 86400..Int.MAX_VALUE -> {
                val d: Int = s1 / 86400
                s1 -= d * 86400
                r += "${d}d "
            }

            else -> s1 = 0
        }
    }
    return if (isNegative) "-$r" else r
}
