package com.fonrouge.fsLib.model.base

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PermissionType(
    val label: String = ""
) {
    @SerialName("1")
    Allow("1"),

    @SerialName("0")
    Deny("0"),

    @SerialName("D")
    Default("D")
}
