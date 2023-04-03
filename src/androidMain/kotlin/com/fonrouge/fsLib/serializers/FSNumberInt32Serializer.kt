package com.fonrouge.fsLib.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Suppress("unused")
actual object FSNumberInt32Serializer : KSerializer<Int> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Number as Int Serializer", PrimitiveKind.DOUBLE)

    override fun deserialize(decoder: Decoder): Int {
        return decoder.decodeInt()

    }

    override fun serialize(encoder: Encoder, value: Int) {
        return encoder.encodeInt(value = value)
    }
}
