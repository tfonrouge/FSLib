package com.fonrouge.fsLib.serializers

import io.kvision.types.OffsetDateTime
import kotlinx.serialization.KSerializer

const val KV_DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS"

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object FSOffsetDateTimeSerializer : KSerializer<OffsetDateTime>
