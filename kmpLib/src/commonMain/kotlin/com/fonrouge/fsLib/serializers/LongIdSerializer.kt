package com.fonrouge.fsLib.serializers

import com.fonrouge.fsLib.types.LongId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object LongIdSerializer : KSerializer<LongId<Any>> {
    override fun deserialize(decoder: Decoder): LongId<Any> {
        return LongId(id = decoder.decodeLong())
    }

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("LongId MP Serializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LongId<Any>) {
        encoder.encodeLong(value.id)
    }
}
