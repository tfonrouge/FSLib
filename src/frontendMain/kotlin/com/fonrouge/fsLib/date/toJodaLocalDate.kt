package com.fonrouge.fsLib.date

import kotlin.js.Date

@Suppress("unused")
val Date.toJodaLocalDate: kotlinx.datetime.internal.JSJoda.LocalDate
    get() = kotlinx.datetime.internal.JSJoda.LocalDate.of(
        year = getFullYear(),
        month = getMonth() + 1,
        dayOfMonth = getDate()
    )
