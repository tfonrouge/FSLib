package com.fonrouge.base.serializers

import io.kvision.types.LocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object FSLocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor
    override fun deserialize(decoder: Decoder): LocalDateTime
    override fun serialize(encoder: Encoder, value: LocalDateTime)
}
