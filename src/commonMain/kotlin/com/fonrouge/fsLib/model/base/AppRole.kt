package com.fonrouge.fsLib.model.base

import com.fonrouge.fsLib.annotations.Collection
import com.fonrouge.fsLib.serializers.OId
import com.fonrouge.fsLib.serializers.XEnum
import com.fonrouge.fsLib.serializers.XEnumSerializer
import kotlinx.serialization.Serializable

@Suppress("unused")
@Collection(name = "__appRole")
@Serializable
class AppRole(
    override val _id: OId<AppRole> = OId(),
    val classOwner: String,
    val funcName: String,
    val roleType: RoleType = RoleType.Simple,
    val description: String?,
    val detail: String?,
    val defaultPermission: PermissionType = PermissionType.Allow
) : BaseDoc<OId<AppRole>> {
    @Serializable
    enum class RoleType(
        override val encoded: String,
        override val label: String
    ) : XEnum {
        Simple("S", "Simple"),
        DataAction("DA", "Data Action"),
    }
}

object RoleTypeSerializer : XEnumSerializer<AppRole.RoleType>() {
    override fun enumEntries(): List<AppRole.RoleType> = AppRole.RoleType.entries
}