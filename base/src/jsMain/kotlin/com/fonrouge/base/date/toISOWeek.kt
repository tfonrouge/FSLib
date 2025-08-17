package com.fonrouge.base.date

import io.kvision.types.LocalDate

@Suppress("unused")
actual val LocalDate.toISOWeek: String
    get() {
        val localDateJsJ = kotlinx.datetime.internal.JSJoda.LocalDate.of(
            year = getFullYear(),
            month = getMonth() + 1,
            dayOfMonth = getDate()
        )
        return "${localDateJsJ.year()}-W${
            localDateJsJ.isoWeekOfWeekyear().toString().padStart(2, '0')
        }"
    }
