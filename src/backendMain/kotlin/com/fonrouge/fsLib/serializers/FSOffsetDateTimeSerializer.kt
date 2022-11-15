package com.fonrouge.fsLib.serializers

import com.github.jershell.kbson.BsonEncoder
import com.github.jershell.kbson.BsonFlexibleDecoder
import io.kvision.types.toOffsetDateTimeF
import io.kvision.types.toStringF
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.litote.kmongo.serialization.OffsetDateTimeSerializer
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Suppress("unused")
actual object FSOffsetDateTimeSerializer : KSerializer<OffsetDateTime> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("OffsetDateTime backEnd Serializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): OffsetDateTime {
        return if (decoder is BsonFlexibleDecoder) {
            val dateTime = decoder.reader.readDateTime()
            OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(dateTime),
                ZoneId.systemDefault()
            )
        } else {
            val decoded = decoder.decodeString()
            if (decoded.contains('T')) {
                decoded.toOffsetDateTimeF()
            } else {
                LocalDateTime
                    .parse(decoded, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    .atZone(ZoneId.systemDefault()).toOffsetDateTime()
            }
        }
    }

    override fun serialize(encoder: Encoder, value: OffsetDateTime) {
        if (encoder is BsonEncoder) {
            encoder.encodeDateTime(OffsetDateTimeSerializer.epochMillis(value))
        } else {
            encoder.encodeString(value.toStringF())
        }
    }
}