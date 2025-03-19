package com.fonrouge.fsLib.date

import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.*

/**
 * builds an [Date] object from ISO8601 week pattern YYYY-Www, resulting date will be start on monday
 */
@Suppress("unused")
actual val String.isoWeekToDate: LocalDate?
    get() = try {
        val year = substring(0..3).toInt()
        val wy = substring(6..7).toLong()
        val localDate = LocalDate.now()
        val weekFields = WeekFields.ISO
        localDate
            .withYear(year)
            .with(weekFields.weekOfYear(), wy)
            .with(weekFields.dayOfWeek(), 1)
    } catch (e: Exception) {
        null
    }
