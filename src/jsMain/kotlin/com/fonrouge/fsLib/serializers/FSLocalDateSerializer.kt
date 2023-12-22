package com.fonrouge.fsLib.serializers

import io.kvision.types.LocalDate
import kotlinx.datetime.internal.JSJoda.convert
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Suppress("unused", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object FSLocalDateSerializer : KSerializer<LocalDate> {
    override fun deserialize(decoder: Decoder): LocalDate {
        return convert(kotlinx.datetime.internal.JSJoda.LocalDate.parse(decoder.decodeString())).toDate()
    }

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("LocalDate Js Serializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDate) {
        /* YYYY-MM-DD */
        encoder.encodeString(value.toISOString().substring(0, 10))
    }
}
