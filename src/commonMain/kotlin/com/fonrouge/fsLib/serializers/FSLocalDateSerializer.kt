package com.fonrouge.fsLib.serializers

import io.kvision.types.LocalDate
import kotlinx.serialization.KSerializer

expect object FSLocalDateSerializer : KSerializer<LocalDate>
