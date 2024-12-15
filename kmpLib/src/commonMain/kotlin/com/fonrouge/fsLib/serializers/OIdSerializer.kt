package com.fonrouge.fsLib.serializers

import com.fonrouge.fsLib.types.OId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object OIdSerializer : KSerializer<OId<Any>> {
    override val descriptor: SerialDescriptor
    override fun deserialize(decoder: Decoder): OId<Any>
    override fun serialize(encoder: Encoder, value: OId<Any>)
}
