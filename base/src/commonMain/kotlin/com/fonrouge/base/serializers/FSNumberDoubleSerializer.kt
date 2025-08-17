package com.fonrouge.base.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object FSNumberDoubleSerializer : KSerializer<Double> {
    override val descriptor: SerialDescriptor
    override fun deserialize(decoder: Decoder): Double
    override fun serialize(encoder: Encoder, value: Double)
}
