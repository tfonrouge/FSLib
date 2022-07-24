package com.fonrouge.fsLib.serializers

import io.kvision.types.LocalDateTime
import kotlinx.serialization.KSerializer

@Suppress("RedundantVisibilityModifier")
public expect object FSLocalDateTimeSerializer : KSerializer<LocalDateTime>
