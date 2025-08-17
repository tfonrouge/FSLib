package com.fonrouge.base.date

import kotlinx.datetime.internal.JSJoda.convert

@Suppress("unused")
val kotlinx.datetime.internal.JSJoda.LocalDate.toDate get() = convert(this).toDate()
