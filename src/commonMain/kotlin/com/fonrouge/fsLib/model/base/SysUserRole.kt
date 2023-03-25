package com.fonrouge.fsLib.model.base

import com.fonrouge.fsLib.annotations.Collection
import com.fonrouge.fsLib.annotations.DontPersist
import com.fonrouge.fsLib.serializers.OId
import kotlinx.serialization.Serializable

@Collection(name = "__userRoles")
@Serializable
class SysUserRole(
    override var _id: OId<SysUserRole> = OId(),
    var sysUser_id: OId<ISysUser>,
    var appRole_id: OId<AppRole>?,
    var permission: PermissionType = PermissionType.Allow
) : BaseDoc<OId<SysUserRole>> {
    @DontPersist
    internal var sysUser: SysUser? = null

    @DontPersist
    var appRole: AppRole? = null
}
