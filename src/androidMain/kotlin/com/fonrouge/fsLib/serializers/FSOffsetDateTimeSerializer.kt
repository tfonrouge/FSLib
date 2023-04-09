package com.fonrouge.fsLib.serializers

import android.annotation.TargetApi
import android.os.Build
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

actual object FSOffsetDateTimeSerializer : KSerializer<OffsetDateTime> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("OffsetDateTime backEnd Serializer", PrimitiveKind.STRING)

    @TargetApi(Build.VERSION_CODES.O)
    override fun deserialize(decoder: Decoder): OffsetDateTime {
        return run {
            val decoded = decoder.decodeString()
            if (decoded.contains('T')) {
                OffsetDateTime.parse(decoded)
            } else {
                LocalDateTime
                    .parse(decoded, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    .atZone(ZoneId.systemDefault()).toOffsetDateTime()
            }
        }
    }

    override fun serialize(encoder: Encoder, value: OffsetDateTime) {
        encoder.encodeString(value.toString())
    }
}