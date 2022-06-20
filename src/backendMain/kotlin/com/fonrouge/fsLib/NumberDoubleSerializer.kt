package com.fonrouge.fsLib

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
public actual object NumberDoubleSerializer : KSerializer<Double> {
    override fun deserialize(decoder: Decoder): Double {
        val bsonDecoder = decoder as BsonFlexibleDecoder
        return when (bsonDecoder.reader.currentBsonType) {
            BsonType.INT32 -> bsonDecoder.decodeInt().toDouble()
            BsonType.INT64 -> bsonDecoder.decodeLong().toDouble()
            BsonType.DOUBLE -> bsonDecoder.decodeDouble()
            else -> {
                throw ServiceException("NumberDoubleSerializer: Unkown how to decode type '${bsonDecoder.reader.currentBsonType}'")
            }
        }
    }

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Number as Double Serializer", PrimitiveKind.DOUBLE)

    override fun serialize(encoder: Encoder, value: Double) {
        return encoder.encodeDouble(value = value)
    }
}
