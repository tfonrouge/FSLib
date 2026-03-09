package com.fonrouge.base

import io.kvision.types.LocalDate
import kotlin.js.Date

/**
 * Retrieves the current date as a `LocalDate` instance in the context of the specific platform.
 *
 * This function is used for obtaining the current date, independent of time and timezone,
 * formatted as a `LocalDate`.
 *
 * @return The current date as a `LocalDate`.
 */
@Suppress("unused")
actual fun localDateNow(): LocalDate = Date()
