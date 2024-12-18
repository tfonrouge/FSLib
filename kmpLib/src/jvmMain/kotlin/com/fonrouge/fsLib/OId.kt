package com.fonrouge.fsLib

import com.fonrouge.fsLib.types.OId
import java.time.OffsetDateTime

@Suppress("unused")
fun <T> OId(offsetDateTime: OffsetDateTime): OId<T> =
    OId(offsetDateTime.toEpochSecond().toString(16).padStart(8, ' ') + "0000000000000000")
