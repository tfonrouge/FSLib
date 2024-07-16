package com.fonrouge.fsLib.date

import java.time.LocalDate
import java.util.Date

/**
 * builds an [Date] object from a string with format YYYY-MM[-DD], day part is optional
 */
@Suppress("unused")
actual val String.yearMonthToDate: LocalDate?
    get() = try {
        val parts = split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val day = parts.getOrNull(2)?.toInt() ?: 1
        LocalDate.of(year, month, day)
    } catch (e: Exception) {
        null
    }
