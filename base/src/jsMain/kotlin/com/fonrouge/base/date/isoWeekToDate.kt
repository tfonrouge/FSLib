package com.fonrouge.base.date

import io.kvision.types.LocalDate
import kotlinx.datetime.internal.JSJoda.convert
import kotlin.js.Date

/**
 * builds an [Date] object from ISO8601 week pattern YYYY-Www, resulting date will be start on monday
 */
@Suppress("unused")
actual val String.isoWeekToDate: LocalDate?
    get() = try {
        val year = substring(0..3).toInt()
        val localDateJsJ = kotlinx.datetime.internal.JSJoda.LocalDate.of(
            year = year,
            month = 1,
            dayOfMonth = 4
        )
        val week = substring(6..7).toInt() - 1
        convert(
            localDateJsJ.plusWeeks(week).let { it.minusDays(it.dayOfWeek().ordinal()) }
        ).toDate()
    } catch (e: Exception) {
        null
    }
