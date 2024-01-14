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

@Suppress("unused", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object FSNumberDoubleSerializer : KSerializer<Double> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Number as Double Serializer", PrimitiveKind.DOUBLE)

    override fun deserialize(decoder: Decoder): Double {
        return if (decoder is BsonFlexibleDecoder) {
            when (decoder.reader.currentBsonType) {
                BsonType.INT32 -> decoder.decodeInt().toDouble()
                BsonType.INT64 -> decoder.decodeLong().toDouble()
                BsonType.DOUBLE -> decoder.decodeDouble()
                else -> {
                    throw ServiceException("NumberDoubleSerializer: Unknown how to decode type '${decoder.reader.currentBsonType}'")
                }
            }
        } else {
            decoder.decodeDouble()
        }
    }

    override fun serialize(encoder: Encoder, value: Double) {
        return encoder.encodeDouble(value = value)
    }
}
