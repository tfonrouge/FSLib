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