package com.fonrouge.fsLib.serializers

import com.fonrouge.fsLib.model.base.BaseModel
import com.github.jershell.kbson.BsonEncoder
import com.github.jershell.kbson.BsonFlexibleDecoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bson.types.ObjectId

actual object IdSerializer : KSerializer<Id<out BaseModel<*>>> {
    override fun deserialize(decoder: Decoder): Id<out BaseModel<*>> {
        return if (decoder is BsonFlexibleDecoder) {
            Id(id = decoder.reader.readObjectId().toHexString())
        } else {
            Id(decoder.decodeString())
        }
    }

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("ObjectId MP Serializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Id<out BaseModel<*>>) {
        if (encoder is BsonEncoder) {
            encoder.encodeObjectId(ObjectId(value.id))
        } else {
            encoder.encodeString(value.id)
        }
    }
}

@Suppress("unused")
fun Id<out BaseModel<*>>?.toObjectId(): ObjectId? {
    return this?.id?.let { ObjectId(it) }
}
