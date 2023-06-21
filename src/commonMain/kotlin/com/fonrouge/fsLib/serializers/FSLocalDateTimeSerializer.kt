package com.fonrouge.fsLib.serializers

import io.kvision.types.LocalDate
import io.kvision.types.LocalDateTime
import kotlinx.serialization.KSerializer

expect object FSLocalDateTimeSerializer : KSerializer<LocalDateTime>
