package com.fonrouge.fsLib.model.base

import com.fonrouge.fsLib.annotations.MongoDoc
import com.fonrouge.fsLib.newObjectId
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@MongoDoc(collection = "__appRoles")
@Serializable
@JsExport
class AppRole(
    override val _id: String = newObjectId(),
    val classOwner: String,
    var funcName: String,
    val roleType: RoleType,
    val description: String?,
    val detail: String?,
    val defaultPermission: PermissionType = PermissionType.Deny
) : BaseModel<String> {
    @Serializable
    enum class RoleType(val description: String) {
        S("Simple"),
        DA("Data Action"),
    }
}
