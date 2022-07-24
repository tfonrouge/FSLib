package com.fonrouge.fsLib.serializers

import io.kvision.types.LocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Suppress("RedundantVisibilityModifier", "unused")
public actual object FSLocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("LocalDateTime frontEnd Serializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDateTime {
        val s = decoder.decodeString()
        return LocalDateTime(s)
    }

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        val s = js("value.toString()") as String
        encoder.encodeString(s)
    }
}
