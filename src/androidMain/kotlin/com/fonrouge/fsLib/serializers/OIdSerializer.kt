package com.fonrouge.fsLib.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object OIdSerializer : KSerializer<OId<Any>> {
    actual override fun deserialize(decoder: Decoder): OId<Any> {
        return OId(id = decoder.decodeString())
    }

    actual override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("ObjectId MP Serializer", PrimitiveKind.STRING)

    actual override fun serialize(encoder: Encoder, value: OId<Any>) {
        encoder.encodeString(value.id)
    }
}
