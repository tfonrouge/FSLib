package com.fonrouge.fsLib.model.base

import com.fonrouge.fsLib.annotations.Collection
import com.fonrouge.fsLib.serializers.OId
import kotlinx.serialization.Serializable

@Collection(collection = "__appRoles")
@Serializable
class AppRole(
    override val _id: OId<AppRole> = OId(),
    val classOwner: String,
    var funcName: String,
    val roleType: RoleType,
    val description: String?,
    val detail: String?,
    val defaultPermission: PermissionType = PermissionType.Deny
) : BaseDoc<OId<AppRole>> {
    @Serializable
    enum class RoleType(val description: String) {
        S("Simple"),
        DA("Data Action"),
    }
}
