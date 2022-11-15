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

@Suppress("unused")
actual object FSObjectIdSerializer : KSerializer<String> {
    override fun deserialize(decoder: Decoder): String {
        return if (decoder is BsonFlexibleDecoder) {
            val objectId = decoder.reader.readObjectId()
            objectId.toHexString()
        } else {
            decoder.decodeString()
        }
    }

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("ObjectId as String Serializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String) {
        if (encoder is BsonEncoder) {
            encoder.encodeObjectId(ObjectId(value))
        } else {
            encoder.encodeString(value)
        }
    }
}