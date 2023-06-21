package com.fonrouge.fsLib.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate

@Suppress("unused")
actual object FSLocalDateSerializer : KSerializer<LocalDate> {
    override fun deserialize(decoder: Decoder): LocalDate {
        val decoded = decoder.decodeString()
        return LocalDate.parse(decoded)
    }

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("LocalDate backEnd Serializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.toString())
    }
}
