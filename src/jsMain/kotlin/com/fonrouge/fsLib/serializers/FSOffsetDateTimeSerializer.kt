package com.fonrouge.fsLib.serializers

import io.kvision.types.OffsetDateTime
import io.kvision.types.toStringF
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.js.Date

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object FSOffsetDateTimeSerializer : KSerializer<OffsetDateTime> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(
            "OffsetDateTime frontEnd Serializer",
            PrimitiveKind.STRING
        )

    override fun deserialize(decoder: Decoder): OffsetDateTime {
        return Date(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: OffsetDateTime) {
        encoder.encodeString(value.toStringF())
    }
}