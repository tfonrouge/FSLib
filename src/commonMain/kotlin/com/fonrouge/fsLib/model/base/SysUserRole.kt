package com.fonrouge.fsLib.model.base

import com.fonrouge.fsLib.annotations.DontPersist
import com.fonrouge.fsLib.annotations.MongoDoc
import com.fonrouge.fsLib.newObjectId
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@MongoDoc(collection = "__userRoles")
@Serializable
@JsExport
class SysUserRole(
    override var _id: String = newObjectId(),
    var sysUser_id: String,
    var appRole_id: String?,
    var permission: PermissionType = PermissionType.Allow
) : BaseModel<String> {
    @DontPersist
    internal var sysUser: SysUser? = null

    @DontPersist
    var appRole: AppRole? = null
}
