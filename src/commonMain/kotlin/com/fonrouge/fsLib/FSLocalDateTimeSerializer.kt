package com.fonrouge.fsLib

import io.kvision.types.LocalDateTime
import kotlinx.serialization.KSerializer

@Suppress("RedundantVisibilityModifier")
public expect object FSLocalDateTimeSerializer : KSerializer<LocalDateTime>
