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
import kotlinx.serialization.json.*
import org.bson.AbstractBsonReader
import org.litote.kmongo.serialization.OffsetDateTimeSerializer
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Serializer for `OffsetDateTime` objects with flexible handling of various formats and input types.
 * This object is designed to support serialization and deserialization of `OffsetDateTime` data in
 * both JSON and BSON contexts.
 *
 * Key features of the serializer:
 * - Supports multiple date-time formats during deserialization, including those with or without millisecond precision.
 * - Handles date-time representations in different string formats, as well as epoch values in milliseconds or seconds.
 * - Adapts to various decoder and encoder types including `BsonFlexibleDecoder`, `JsonDecoder`, and standard decoders.
 * - Automatically adjusts to the system's default time zone for locale-specific operations.
 *
 * Supported Input Formats:
 * - ISO 8601 date-time strings (e.g., "2023-08-15T14:30:00Z").
 * - Non-standard date-time strings with or without millisecond precision.
 * - Numeric epoch values (interpreted as seconds or milliseconds based on their magnitude).
 *
 * Public API:
 * - `decodeFromString`: Parses a string into an `OffsetDateTime` object. Automatically determines the format and precision of the input.
 * - Overriden methods from `KSerializer` to provide functionality for encoding and decoding `OffsetDateTime` objects.
 *
 * Internal Behavior:
 * - Utilizes specific date-time patterns to interpret input strings, such as `yyyy-MM-dd HH:mm:ss.S` for millisecond precision or `yyyy-MM-dd HH:mm:ss` otherwise.
 * - Converts numeric epoch values to `OffsetDateTime` using heuristics to differentiate between seconds and milliseconds.
 * - Handles BSON-specific decoding by reading date-times directly from BSON data where applicable.
 *
 * Usage Scope:
 * This serializer is suitable for use cases involving mixed data formats or systems that require interoperability
 * between JSON and BSON data representations. It can seamlessly operate in both, allowing smooth conversions
 * and accurate interpretations of input data.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object FSOffsetDateTimeSerializer : KSerializer<OffsetDateTime> {
    private const val FORMAT_WITH_MILLIS = "yyyy-MM-dd HH:mm:ss.S"
    private const val FORMAT_SECONDS = "yyyy-MM-dd HH:mm:ss"

    private fun parseLocalDateTime(decoded: String, pattern: String): OffsetDateTime {
        val formatter = DateTimeFormatter.ofPattern(pattern)
        return LocalDateTime.parse(decoded.substring(0, pattern.length), formatter)
            .atZone(ZoneId.systemDefault())
            .toOffsetDateTime()
    }

    fun decodeFromString(decoded: String): OffsetDateTime = when {
        decoded.contains('T') -> decoded.toOffsetDateTimeF()
        decoded.length > 18 -> {
            val format = if (decoded.length > 19) FORMAT_WITH_MILLIS else FORMAT_SECONDS
            parseLocalDateTime(decoded, format)
        }

        else -> {
            val value = decoded.toLong()
            // Heuristic: values < 10^11 are likely seconds (e.g., year 2026 in seconds is ~1.7*10^9)
            val instant = if (value < 100_000_000_000L) {
                Instant.ofEpochSecond(value)
            } else {
                Instant.ofEpochMilli(value)
            }
            OffsetDateTime.ofInstant(instant, ZoneId.systemDefault())
        }
    }

    actual override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("OffsetDateTime backEnd Serializer", PrimitiveKind.STRING)

    actual override fun deserialize(decoder: Decoder): OffsetDateTime = if (decoder is BsonFlexibleDecoder) {
        val dateTime = decoder.reader.readDateTime()
        OffsetDateTime.ofInstant(
            Instant.ofEpochMilli(dateTime),
            ZoneId.systemDefault()
        )
    } else {
        when (decoder) {
            is FlexibleDecoder -> {
                val decoded = if (decoder.reader.state == AbstractBsonReader.State.NAME) {
                    decoder.decodeLong()
                } else {
                    decoder.reader.readDateTime()
                }
                OffsetDateTime.ofInstant(
                    Instant.ofEpochMilli(decoded),
                    ZoneId.systemDefault()
                )
            }

            is JsonDecoder -> {
                when (val jsonElement = decoder.decodeJsonElement()) {
                    is JsonPrimitive -> {
                        decodeFromString(jsonElement.content)
                    }

                    is JsonNull -> throw kotlinx.serialization.SerializationException(
                        "Cannot deserialize OffsetDateTime from JSON null"
                    )

                    is JsonArray -> throw kotlinx.serialization.SerializationException(
                        "Cannot deserialize OffsetDateTime from JSON array: $jsonElement"
                    )

                    is JsonObject -> throw kotlinx.serialization.SerializationException(
                        "Cannot deserialize OffsetDateTime from JSON object: $jsonElement"
                    )
                }
            }

            else -> {
                val decoded = decoder.decodeString()
                decodeFromString(decoded)
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