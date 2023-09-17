package com.fonrouge.fsLib.serializers

import io.kvision.types.OffsetDateTime
import kotlinx.serialization.KSerializer

const val KV_DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS"

expect object FSOffsetDateTimeSerializer : KSerializer<OffsetDateTime>
