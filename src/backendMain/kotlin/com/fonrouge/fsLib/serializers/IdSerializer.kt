package com.fonrouge.fsLib.serializers

import com.github.jershell.kbson.BsonEncoder
import com.github.jershell.kbson.BsonFlexibleDecoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bson.types.ObjectId

actual object IdSerializer : KSerializer<Id<Any>> {
    override fun deserialize(decoder: Decoder): Id<Any> {
        return if (decoder is BsonFlexibleDecoder) {
            Id(id = decoder.reader.readObjectId().toHexString())
        } else {
            Id(decoder.decodeString())
        }
    }

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("ObjectId MP Serializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Id<Any>) {
        if (encoder is BsonEncoder) {
            encoder.encodeObjectId(ObjectId(value.id))
        } else {
            encoder.encodeString(value.id)
        }
    }
}

@Suppress("unused")
fun Id<Any>?.toObjectId(): ObjectId? {
    return this?.id?.let { ObjectId(it) }
}
