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
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Suppress("RedundantVisibilityModifier", "unused")
public actual object FSLocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("LocalDateTime backEnd Serializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return if (decoder is BsonFlexibleDecoder) {
            LocalDateTime.ofInstant(
                Instant.ofEpochMilli(decoder.reader.readDateTime()),
                ZoneOffset.UTC
            )
        } else {
            LocalDateTime.parse(decoder.decodeString(), DateTimeFormatter.ISO_DATE_TIME)
        }
    }

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        if (encoder is BsonEncoder) {
            encoder.encodeDateTime(ZonedDateTimeSerializer.epochMillis(value.atZone(ZoneOffset.UTC)))
        } else {
            encoder.encodeString(value.format(DateTimeFormatter.ISO_DATE_TIME) + "Z")
        }
    }
}
