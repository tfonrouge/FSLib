package com.fonrouge.base.date

import kotlin.js.Date

/**
 * builds an [Date] object from a string with format YYYY-MM[-DD], day part is optional
 */
@Suppress("unused")
actual val String.yearMonthToDate: Date?
    get() = try {
        val parts = split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt() - 1
        val day = parts.getOrNull(2)?.toInt() ?: 1
        Date(year, month, day)
    } catch (e: Exception) {
        null
    }
