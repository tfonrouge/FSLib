package com.fonrouge.base.serializers

import com.github.jershell.kbson.BsonEncoder
import com.github.jershell.kbson.BsonFlexibleDecoder
import com.github.jershell.kbson.FlexibleDecoder
import io.kvision.types.toOffsetDateTimeF
import io.kvision.types.toStringF
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bson.AbstractBsonReader
import org.litote.kmongo.serialization.OffsetDateTimeSerializer
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Serializer for the `OffsetDateTime` type which supports encoding and decoding into multiple formats.
 *
 * This serializer is designed to handle various serialization scenarios,
 * such as working with BSON readers and encoders, as well as text-based formats.
 *
 * The `deserialize` function supports:
 * - BSON flexible decoders, extracting date-time from epoch milliseconds.
 * - Map decoders by decoding a long value representing epoch milliseconds.
 * - Text-based formats, interpreting strings either in an ISO-like format
 *   or custom date-time formats when a 'T' character is absent.
 *
 * The `serialize` function handles:
 * - BSON encoders by converting `OffsetDateTime` to epoch milliseconds.
 * - String-based formats by converting `OffsetDateTime` to string representations.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object FSOffsetDateTimeSerializer : KSerializer<OffsetDateTime> {
    actual override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("OffsetDateTime backEnd Serializer", PrimitiveKind.STRING)

    actual override fun deserialize(decoder: Decoder): OffsetDateTime {
        return if (decoder is BsonFlexibleDecoder) {
            val dateTime = decoder.reader.readDateTime()
            OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(dateTime),
                ZoneId.systemDefault()
            )
        } else {
            if (decoder is FlexibleDecoder) { // MapDecoder
                val decoded = if (decoder.reader.state == AbstractBsonReader.State.NAME) {
                    decoder.decodeLong()
                } else {
                    decoder.reader.readDateTime()
                }
                OffsetDateTime.ofInstant(
                    Instant.ofEpochMilli(decoded),
                    ZoneId.systemDefault()
                )
            } else {
                val decoded = decoder.decodeString()
                if (decoded.contains('T')) {
                    decoded.toOffsetDateTimeF()
                } else {
                    val format = when {
                        decoded.length > 19 -> "yyyy-MM-dd HH:mm:ss.S"
                        else -> "yyyy-MM-dd HH:mm:ss"
                    }
                    LocalDateTime
                        .parse(decoded.substring(0, format.length), DateTimeFormatter.ofPattern(format))
                        .atZone(ZoneId.systemDefault()).toOffsetDateTime()
                }
            }
        }
    }

    actual override fun serialize(encoder: Encoder, value: OffsetDateTime) {
        if (encoder is BsonEncoder) {
            encoder.encodeDateTime(OffsetDateTimeSerializer.epochMillis(value))
        } else {
            encoder.encodeString(value.toStringF())
        }
    }
}