package com.fonrouge.fsLib.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object FSLocalDateTimeSerializer : KSerializer<LocalDateTime> {
    actual override fun deserialize(decoder: Decoder): LocalDateTime {
        val decoded = decoder.decodeString()
        return if (decoded[10] == ' ') {
            LocalDateTime.parse(decoded.substring(0..9) + "T" + decoded.substring(11))
        } else
            LocalDateTime.parse(decoded)
    }

    actual override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("LocalDate backEnd Serializer", PrimitiveKind.STRING)

    actual override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.toString())
    }
}
