package com.fonrouge.fsLib.model.base

import com.fonrouge.fsLib.serializers.XEnum
import com.fonrouge.fsLib.serializers.XEnumSerializer
import kotlinx.serialization.Serializable

@Serializable(with = PermissionTypeSerializer::class)
enum class PermissionType(
    override val encoded: String,
    override val label: String = ""
) : XEnum {
    Allow("1"),
    Deny("0"),
    Default("D")
}

object PermissionTypeSerializer : XEnumSerializer<PermissionType>() {
    override fun enumEntries(): List<PermissionType> = PermissionType.entries
}
