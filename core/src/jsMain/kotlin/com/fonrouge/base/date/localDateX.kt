package com.fonrouge.base.date

import kotlinx.datetime.LocalDate
import kotlin.js.Date

/**
 * Converts a JavaScript [Date] instance to a [kotlinx.datetime.LocalDate] instance.
 *
 * @return a [kotlinx.datetime.LocalDate] representing the same year, month, and day as the original [Date].
 */
@Suppress("unused")
fun Date.dateToLocalDateX(): LocalDate =
    LocalDate(year = this.getFullYear(), monthNumber = this.getMonth() + 1, dayOfMonth = this.getDate())

/**
 * Converts a [kotlinx.datetime.LocalDate] instance to a JavaScript [Date] instance.
 *
 * @return a [Date] representing the same year, month, and day as the original [kotlinx.datetime.LocalDate].
 */
@Suppress("unused")
fun LocalDate.localDateXtoDate(): Date =
    Date(year = this.year, month = this.monthNumber - 1, day = this.dayOfMonth)
