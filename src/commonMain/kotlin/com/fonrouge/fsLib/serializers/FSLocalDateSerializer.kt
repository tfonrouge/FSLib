package com.fonrouge.fsLib.serializers

import io.kvision.types.LocalDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object FSLocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor
    override fun deserialize(decoder: Decoder): LocalDate
    override fun serialize(encoder: Encoder, value: LocalDate)
}
