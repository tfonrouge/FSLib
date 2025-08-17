package com.fonrouge.base.lib

import kotlinx.datetime.internal.JSJoda.LocalDate
import kotlinx.datetime.internal.JSJoda.convert
import kotlin.js.Date

val Date.firstDayOfDateWeek
    get() = LocalDate.of(
        year = getFullYear(),
        month = getMonth() + 1,
        dayOfMonth = getDate()
    ).let { it.minusDays(it.dayOfWeek().ordinal()) }

@Suppress("unused")
val Date.toWeekRange: String
    get() {
        val localDateJsJ: LocalDate = firstDayOfDateWeek
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
