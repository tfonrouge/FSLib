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
import java.time.OffsetDateTime

@Suppress("unused")
const val EMPTY_OID = "000000000000000000000000"

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object OIdSerializer : KSerializer<OId<Any>> {
    actual override fun deserialize(decoder: Decoder): OId<Any> {
        return if (decoder is BsonFlexibleDecoder) {
            OId(id = decoder.reader.readObjectId().toHexString())
        } else {
            OId(decoder.decodeString())
        }
    }

    actual override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("ObjectId MP Serializer", PrimitiveKind.STRING)

    actual override fun serialize(encoder: Encoder, value: OId<Any>) {
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

@Suppress("unused")
fun <T> OId(offsetDateTime: OffsetDateTime): OId<T> =
    OId(offsetDateTime.toEpochSecond().toString(16).padStart(8, ' ') + "0000000000000000")
