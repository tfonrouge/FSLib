package com.fonrouge.fsLib.serializers

import com.github.jershell.kbson.BsonFlexibleDecoder
import io.kvision.remote.ServiceException
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bson.BsonType

@Suppress("unused", "RedundantVisibilityModifier")
public actual object FSObjectIdSerializer : KSerializer<String> {
    override fun deserialize(decoder: Decoder): String {
        val bsonDecoder = decoder as BsonFlexibleDecoder
        return when (bsonDecoder.reader.currentBsonType) {
            BsonType.OBJECT_ID -> bsonDecoder.reader.readObjectId().toHexString()
            else -> {
                throw ServiceException("FSObjectIdSerializer: Unknown how to decode type '${bsonDecoder.reader.currentBsonType}'")
            }
        }
    }

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Binary as String Serializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String) {
        return encoder.encodeString(value = value)
    }
}
