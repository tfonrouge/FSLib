package com.fonrouge.fsLib.serializers

import com.github.jershell.kbson.BsonEncoder
import com.github.jershell.kbson.BsonFlexibleDecoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.litote.kmongo.serialization.ZonedDateTimeSerializer
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Suppress("RedundantVisibilityModifier", "unused", "MemberVisibilityCanBePrivate")
public actual object FSLocalDateTimeSerializer : KSerializer<LocalDateTime> {

    const val KV_DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss"

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("LocalDateTime backEnd Serializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return if (decoder is BsonFlexibleDecoder) {
            LocalDateTime.ofInstant(
                Instant.ofEpochMilli(decoder.reader.readDateTime()),
                ZoneId.systemDefault()
            )
        } else {
            LocalDateTime.parse(decoder.decodeString(), DateTimeFormatter.ofPattern(KV_DEFAULT_DATETIME_FORMAT))
        }
    }

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        if (encoder is BsonEncoder) {
            encoder.encodeDateTime(ZonedDateTimeSerializer.epochMillis(value.atZone(ZoneId.systemDefault())))
        } else {
            encoder.encodeString(value.format(DateTimeFormatter.ofPattern(KV_DEFAULT_DATETIME_FORMAT)))
        }
    }
}
