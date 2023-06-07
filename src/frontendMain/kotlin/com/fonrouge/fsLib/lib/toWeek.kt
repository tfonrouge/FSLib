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
val Date.toWeek: String
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

@Suppress("unused")
/* TODO: use a more precise/std method */
fun isoWeekToDate(isoWeek: String): Date {
    val year = isoWeek.substring(0..3).toInt()
    val localDateJsJ = LocalDate.of(
        year = year,
        month = 1,
        dayOfMonth = 4
    )
    val week = isoWeek.substring(6..7).toInt() - 1
    return convert(localDateJsJ.plusWeeks(week)).toDate()
}
