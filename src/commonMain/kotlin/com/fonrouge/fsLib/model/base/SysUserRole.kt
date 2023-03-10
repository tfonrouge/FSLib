package com.fonrouge.fsLib.model.base

import com.fonrouge.fsLib.annotations.DontPersist
import com.fonrouge.fsLib.annotations.MongoDoc
import com.fonrouge.fsLib.serializers.Id
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@MongoDoc(collection = "__userRoles")
@Serializable
@JsExport
class SysUserRole(
    override var _id: Id<SysUserRole> = Id(),
    var sysUser_id: Id<ISysUser>,
    var appRole_id: String?,
    var permission: PermissionType = PermissionType.Allow
) : BaseModel<Id<SysUserRole>> {
    @DontPersist
    internal var sysUser: SysUser? = null

    @DontPersist
    var appRole: AppRole? = null
}
