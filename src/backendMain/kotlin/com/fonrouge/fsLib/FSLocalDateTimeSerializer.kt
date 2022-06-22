package com.fonrouge.fsLib

import com.github.jershell.kbson.BsonFlexibleDecoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Suppress("RedundantVisibilityModifier", "unused")
public actual object FSLocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("LocalDateTime backEnd Serializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDateTime {
        val bsonDecoder = decoder as BsonFlexibleDecoder
        return LocalDateTime.ofInstant(
            Instant.ofEpochMilli(bsonDecoder.reader.readDateTime()),
            TimeZone.getDefault().toZoneId()
        )
    }

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        val s = value.format(DateTimeFormatter.ISO_DATE_TIME)
        encoder.encodeString(s)
    }
}
