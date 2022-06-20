package com.fonrouge.fsLib

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Suppress("unused", "RedundantVisibilityModifier")
public actual object NumberDoubleSerializer : KSerializer<Double> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Number as Double Serializer", PrimitiveKind.DOUBLE)

    override fun deserialize(decoder: Decoder): Double {
        return decoder.decodeDouble()
    }

    override fun serialize(encoder: Encoder, value: Double) {
        encoder.encodeDouble(value = value)
    }
}
