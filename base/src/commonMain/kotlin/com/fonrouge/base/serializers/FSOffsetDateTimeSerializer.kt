package com.fonrouge.base.serializers

import io.kvision.types.OffsetDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

const val KV_DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS"

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object FSOffsetDateTimeSerializer : KSerializer<OffsetDateTime> {
    override val descriptor: SerialDescriptor
    override fun deserialize(decoder: Decoder): OffsetDateTime
    override fun serialize(encoder: Encoder, value: OffsetDateTime)
}
