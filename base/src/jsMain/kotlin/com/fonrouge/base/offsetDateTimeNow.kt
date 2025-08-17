package com.fonrouge.base

import io.kvision.types.OffsetDateTime
import kotlin.js.Date

/**
 * Retrieves the current date and time as an `OffsetDateTime` instance, specific to the platform.
 *
 * This function provides a standardized way to obtain the current date and time,
 * including the offset from UTC, formatted as an `OffsetDateTime`.
 *
 * @return The current `OffsetDateTime` representing the present date and time with offset.
 */
@Suppress("RedundantVisibilityModifier")
public actual fun offsetDateTimeNow(): OffsetDateTime = Date()
