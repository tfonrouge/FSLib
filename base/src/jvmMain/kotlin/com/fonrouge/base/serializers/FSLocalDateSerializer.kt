package com.fonrouge.base.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object FSLocalDateSerializer : KSerializer<LocalDate> {
    actual override fun deserialize(decoder: Decoder): LocalDate {
        val decoded = decoder.decodeString()
        return LocalDate.parse(decoded.substring(0..9))
    }

    actual override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("LocalDate backEnd Serializer", PrimitiveKind.STRING)

    actual override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.toString())
    }
}
