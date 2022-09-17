package com.fonrouge.fsLib.model.base

import com.fonrouge.fsLib.annotations.DontPersist
import com.fonrouge.fsLib.annotations.MongoDoc
import com.fonrouge.fsLib.newObjectId
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@MongoDoc(collection = "__userRoles")
@Serializable
@JsExport
class AppUserRole(
    override var _id: String = newObjectId(),
    var appUser_id: String,
    var appRole_id: String?,
    var permission: PermissionType = PermissionType.Allow
) : BaseModel<String> {
    @DontPersist
    var appUser: AppUser? = null

    @DontPersist
    var appRole: AppRole? = null
}
