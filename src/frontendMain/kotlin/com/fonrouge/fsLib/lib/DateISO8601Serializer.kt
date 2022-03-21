package com.fonrouge.fsLib.lib

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.js.Date

object DateISO8601Serializer : KSerializer<Date> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ISO8601")

    override fun deserialize(decoder: Decoder): Date {
        return Date(Date.parse(decoder.decodeString()))
    }

    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeString(value.toISOString())
    }
}
