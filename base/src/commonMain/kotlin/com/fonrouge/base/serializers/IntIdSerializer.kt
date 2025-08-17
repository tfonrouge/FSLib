package com.fonrouge.base.serializers

import com.fonrouge.base.types.IntId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object IntIdSerializer : KSerializer<IntId<Any>> {
    override fun deserialize(decoder: Decoder): IntId<Any> {
        return IntId(id = decoder.decodeInt())
    }

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("IntId MP Serializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: IntId<Any>) {
        encoder.encodeInt(value.id)
    }
}
