package com.fonrouge.fsLib.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Suppress("unused")
actual object FSObjectIdSerializer : KSerializer<String> {
    override fun deserialize(decoder: Decoder): String {
        return decoder.decodeString()
    }

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("ObjectId frontEnd Serializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }
}