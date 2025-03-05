package com.fonrouge.fsLib

import java.time.OffsetDateTime
import java.time.ZoneId

@Suppress("unused")
actual fun offsetDateTimeNow(): OffsetDateTime = OffsetDateTime.now(ZoneId.systemDefault())
