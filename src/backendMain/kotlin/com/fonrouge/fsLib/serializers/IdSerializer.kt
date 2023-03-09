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
import org.bson.BsonDocument
import org.bson.BsonObjectId
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.path
import kotlin.reflect.KProperty

actual object IdSerializer : KSerializer<Id<BaseModel<*>>> {
    override fun deserialize(decoder: Decoder): Id<BaseModel<*>> {
        return if (decoder is BsonFlexibleDecoder) {
            val objectId = decoder.reader.readObjectId()
            Id(id = objectId.toHexString())
        } else {
            Id(decoder.decodeString())
        }
    }

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("ObjectId MP Serializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Id<BaseModel<*>>) {
        if (encoder is BsonEncoder) {
            encoder.encodeObjectId(ObjectId(value.id))
        } else {
            val s = "{\"\$oid\": \"${value.id}\"}"
            encoder.encodeString(s)
        }
    }
}

/**
 * Equality filter to use with the [Id] ObjectId container
 *
 * @param value - the [Id] value
 */
@Suppress("unused")
infix fun KProperty<Id<*>?>.eqId(value: Id<*>?): Bson = BsonDocument(path(), BsonObjectId(ObjectId(value?.id)))
