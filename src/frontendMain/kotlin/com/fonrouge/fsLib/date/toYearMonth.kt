package com.fonrouge.fsLib.date

import io.kvision.types.LocalDate

@Suppress("unused")
actual val LocalDate.toYearMonth: String get() = "${getFullYear()}-${getMonth().inc().toString().padStart(2, '0')}"
