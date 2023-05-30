package com.fonrouge.fsLib.serializers

import io.kvision.types.LocalDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.js.Date

actual object FSLocalDate : KSerializer<LocalDate> {
    override fun deserialize(decoder: Decoder): LocalDate {
        return Date(decoder.decodeString())
    }

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("LocalDate Js Serializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDate) {
        /* YYYY-MM-DD */
        encoder.encodeString(value.toISOString().substring(0, 10))
    }
}
