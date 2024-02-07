package com.fonrouge.fsLib.model.base

import com.fonrouge.fsLib.serializers.OId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("unused")
interface IAppRole : BaseDoc<OId<IAppRole>> {
    val classOwner: String
    val funcName: String
    val roleType: RoleType
    val description: String?
    val detail: String?
    val defaultPermission: PermissionType

    @Serializable
    enum class RoleType(
        val label: String
    ) {
        @SerialName("S")
        Simple("Simple"),

        @SerialName("DA")
        DataAction("Data Action"),
    }
}
