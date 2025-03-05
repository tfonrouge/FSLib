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
actual object FSNumberInt32Serializer : KSerializer<Int> {
    actual override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Number as Int Serializer", PrimitiveKind.DOUBLE)

    actual override fun deserialize(decoder: Decoder): Int {
        return if (decoder is BsonFlexibleDecoder) {
            when (decoder.reader.currentBsonType) {
                BsonType.INT32 -> decoder.decodeInt()
                BsonType.INT64 -> decoder.decodeLong().toInt()
                BsonType.DOUBLE -> decoder.decodeDouble().toInt()
                else -> {
                    throw ServiceException("NumberIntSerializer: Unknown how to decode type '${decoder.reader.currentBsonType}'")
                }
            }
        } else {
            decoder.decodeInt()
        }
    }

    actual override fun serialize(encoder: Encoder, value: Int) {
        return encoder.encodeInt(value = value)
    }
}
