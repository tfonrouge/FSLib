package com.fonrouge.base

import java.time.OffsetDateTime

/**
 * Retrieves the current date and time with an offset from UTC, based on the system's default time zone.
 *
 * This function provides a timestamp with timezone information using the `OffsetDateTime` class.
 *
 * @return The current date and time as an `OffsetDateTime` instance.
 */
@Suppress("RedundantVisibilityModifier")
public actual fun offsetDateTimeNow(): OffsetDateTime = OffsetDateTime.now()
