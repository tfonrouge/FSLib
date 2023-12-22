package com.fonrouge.fsLib.serializers

import io.kvision.types.LocalDateTime
import kotlinx.serialization.KSerializer

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object FSLocalDateTimeSerializer : KSerializer<LocalDateTime>
