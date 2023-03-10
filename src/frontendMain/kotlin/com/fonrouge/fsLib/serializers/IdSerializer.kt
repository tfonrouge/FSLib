package com.fonrouge.fsLib.serializers

import com.fonrouge.fsLib.model.base.BaseModel
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

actual object IdSerializer : KSerializer<Id<out BaseModel<*>>> {
    override fun deserialize(decoder: Decoder): Id<out BaseModel<*>> {
        return Id(id = decoder.decodeString())
    }

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("ObjectId MP Serializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Id<out BaseModel<*>>) {
        encoder.encodeString(value.id)
    }
}
