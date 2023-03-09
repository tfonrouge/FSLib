package com.fonrouge.fsLib.serializers

import com.fonrouge.fsLib.model.base.BaseModel
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

actual object FSIdSerializer : KSerializer<FSId<BaseModel<*>>> {
    override fun deserialize(decoder: Decoder): FSId<BaseModel<*>> {
        return FSId(id = decoder.decodeString())
    }

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("ObjectId MP Serializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: FSId<BaseModel<*>>) {
        encoder.encodeString(value.id)
    }
}
