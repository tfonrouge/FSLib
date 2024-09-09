package com.fonrouge.fsLib.model.base

import com.fonrouge.fsLib.enums.XEnum
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PermissionType(override val encoded: String) : XEnum {
    @SerialName("1")
    Allow("1"),

    @SerialName("0")
    Deny("0"),

    @SerialName("D")
    Default("D")
}
