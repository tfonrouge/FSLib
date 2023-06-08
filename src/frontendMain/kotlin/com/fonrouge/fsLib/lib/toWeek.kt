package com.fonrouge.fsLib.lib

import kotlinx.datetime.internal.JSJoda.LocalDate
import kotlinx.datetime.internal.JSJoda.convert
import kotlin.js.Date

fun firstDayOfDateWeek(date: Date) = LocalDate.of(
    year = date.getFullYear(),
    month = date.getMonth() + 1,
    dayOfMonth = date.getDate()
).let { it.minusDays(it.dayOfWeek().ordinal()) }

@Suppress("unused")
val Date.toWeekRange: String
    get() {
        val localDateJsJ: LocalDate = firstDayOfDateWeek(this)
        val opt = dateLocaleOptions {
            weekday = "short"
            day = "numeric"
            month = "short"
            year = "numeric"
        }
        val l1 = convert(localDateJsJ).toDate().toLocaleDateString("es-MX", opt)
        val l2 = convert(localDateJsJ.plusDays(6)).toDate().toLocaleDateString("es-MX", opt)
        return "${localDateJsJ.isoWeekOfWeekyear()} ($l1 - $l2)"
    }

@Suppress("unused")
val Date.toISOWeek: String
    get() {
        val localDateJsJ = LocalDate.of(
            year = getFullYear(),
            month = getMonth() + 1,
            dayOfMonth = getDate()
        )
        return "${localDateJsJ.year()}-W${localDateJsJ.isoWeekOfWeekyear()}"
    }

/**
 * builds an [Date] object from ISO8601 week pattern YYYY-Www, resulting date will be start on monday
 */
@Suppress("unused")
        /* TODO: use a more precise/std method */
val String.isoWeekToDate: Date?
    get() = try {
        val year = substring(0..3).toInt()
        val localDateJsJ = LocalDate.of(
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

/**
 * builds an [Date] object from a string with format YYYY-MM[-DD], day part is optional
 */
@Suppress("unused")
val String.yearMonthToDate: Date?
    get() = try {
        val parts = split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt() - 1
        val day = parts.getOrNull(2)?.toInt() ?: 1
        Date(year, month, day)
    } catch (e: Exception) {
        null
    }
