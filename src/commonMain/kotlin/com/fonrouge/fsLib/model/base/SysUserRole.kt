package com.fonrouge.fsLib.model.base

import com.fonrouge.fsLib.annotations.DontPersist
import com.fonrouge.fsLib.annotations.MongoDoc
import com.fonrouge.fsLib.serializers.OId
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@MongoDoc(collection = "__userRoles")
@Serializable
@JsExport
class SysUserRole(
    override var _id: OId<SysUserRole> = OId(),
    var sysUser_id: OId<ISysUser>,
    var appRole_id: OId<AppRole>?,
    var permission: PermissionType = PermissionType.Allow
) : BaseModel<OId<SysUserRole>> {
    @DontPersist
    internal var sysUser: SysUser? = null

    @DontPersist
    var appRole: AppRole? = null
}
