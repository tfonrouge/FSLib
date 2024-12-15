package com.fonrouge.fsLib.serializers

import com.fonrouge.fsLib.types.StringId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object StringIdSerializer : KSerializer<StringId<Any>> {
    override fun deserialize(decoder: Decoder): StringId<Any> {
        return StringId(id = decoder.decodeString())
    }

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("StringId MP Serializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: StringId<Any>) {
        encoder.encodeString(value.id)
    }
}
