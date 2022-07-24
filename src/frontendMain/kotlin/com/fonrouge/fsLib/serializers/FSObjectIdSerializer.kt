package com.fonrouge.fsLib.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Suppress("RedundantVisibilityModifier")
public actual object FSObjectIdSerializer : KSerializer<String> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ObjectId as String Serializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String {
        return decoder.decodeString()
    }

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value = value)
    }
}
