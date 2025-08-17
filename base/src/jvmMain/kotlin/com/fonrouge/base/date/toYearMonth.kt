package com.fonrouge.base.date

import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Suppress("unused")
actual val LocalDate.toYearMonth: String get() = format(DateTimeFormatter.ofPattern("yyyy-MM"))
