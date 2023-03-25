package com.fonrouge.fsLib.model.base

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = PermissionTypeSerializer::class)
enum class PermissionType(val id: String) {
    Allow("1"),
    Deny("0"),
    Default("D"),
}

object PermissionTypeSerializer : KSerializer<PermissionType> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("PermissionType", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: PermissionType) {
        encoder.encodeString(value = value.id)
    }

    override fun deserialize(decoder: Decoder): PermissionType {
        val code = decoder.decodeString()
        return PermissionType.values().first { it.id == code }
    }
}
