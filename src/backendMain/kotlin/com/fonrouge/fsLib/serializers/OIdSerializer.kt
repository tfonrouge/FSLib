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

actual object OIdSerializer : KSerializer<OId<Any>> {
    override fun deserialize(decoder: Decoder): OId<Any> {
        return if (decoder is BsonFlexibleDecoder) {
            OId(id = decoder.reader.readObjectId().toHexString())
        } else {
            OId(decoder.decodeString())
        }
    }

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("ObjectId MP Serializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: OId<Any>) {
        if (encoder is BsonEncoder) {
            encoder.encodeObjectId(ObjectId(value.id))
        } else {
            encoder.encodeString(value.id)
        }
    }
}

@Suppress("unused")
fun OId<Any>?.toObjectId(): ObjectId? {
    return this?.id?.let { ObjectId(it) }
}
