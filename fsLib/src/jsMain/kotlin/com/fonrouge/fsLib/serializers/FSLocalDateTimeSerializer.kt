package com.fonrouge.fsLib.serializers

import io.kvision.types.LocalDateTime
import kotlinx.datetime.internal.JSJoda.LocalDate
import kotlinx.datetime.internal.JSJoda.convert
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Suppress("unused", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object FSLocalDateTimeSerializer : KSerializer<LocalDateTime> {
    actual override fun deserialize(decoder: Decoder): LocalDateTime {
        return convert(LocalDate.parse(decoder.decodeString())).toDate()
    }

    actual override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("LocalDateTime Js Serializer", PrimitiveKind.STRING)

    actual override fun serialize(encoder: Encoder, value: LocalDateTime) {
        /* YYYY-MM-DD */
        encoder.encodeString(value.toISOString())
    }
}