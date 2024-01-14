package com.fonrouge.fsLib.model.base

import com.fonrouge.fsLib.serializers.OId
import com.fonrouge.fsLib.serializers.XEnum
import com.fonrouge.fsLib.serializers.XEnumSerializer
import kotlinx.serialization.Serializable

@Suppress("unused")
interface IAppRole : BaseDoc<OId<IAppRole>> {
    val classOwner: String
    val funcName: String
    val roleType: RoleType
    val description: String?
    val detail: String?
    val defaultPermission: PermissionType

    @Serializable(with = RoleTypeSerializer::class)
    enum class RoleType(
        override val encoded: String,
        override val label: String
    ) : XEnum {
        Simple("S", "Simple"),
        DataAction("DA", "Data Action"),
    }
}

object RoleTypeSerializer : XEnumSerializer<IAppRole.RoleType>() {
    override fun enumEntries(): List<IAppRole.RoleType> = IAppRole.RoleType.entries
}