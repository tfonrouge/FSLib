package com.fonrouge.base.date

import java.time.LocalDate
import java.time.temporal.WeekFields

@Suppress("unused")
actual val LocalDate.toISOWeek: String
    get() {
        val wy = get(WeekFields.ISO.weekOfYear())
        return "${year}-W${wy.toString().padStart(2, '0')}"
    }
