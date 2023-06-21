package com.fonrouge.fsLib.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime

actual object FSLocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override fun deserialize(decoder: Decoder): LocalDateTime {
        val decoded = decoder.decodeString()
        return LocalDateTime.parse(decoded)
    }

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("LocalDate backEnd Serializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.toString())
    }
}