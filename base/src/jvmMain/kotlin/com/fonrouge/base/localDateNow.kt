package com.fonrouge.base

import java.time.LocalDate

/**
 * Retrieves the current date based on the system's default time zone.
 *
 * This function provides the current date without time information,
 * utilizing the `LocalDate` class.
 *
 * @return The current date as a `LocalDate` instance.
 */
@Suppress("unused")
actual fun localDateNow(): LocalDate = LocalDate.now()
