package com.fonrouge.fsLib.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

interface XEnum {
    val encoded: String
    val label: String
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
fun XEnum.toString(): String {
    return encoded
}

@Suppress("unused")
abstract class XEnumSerializer<T : XEnum> : KSerializer<T> {
    abstract fun enumEntries(): List<T>
    final override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("XEnum", PrimitiveKind.STRING)

    final override fun deserialize(decoder: Decoder): T {
        val d = decoder.decodeString()
        return enumEntries().first { it.encoded == d }
    }

    final override fun serialize(encoder: Encoder, value: T) {
        encoder.encodeString(value.encoded)
    }
}
