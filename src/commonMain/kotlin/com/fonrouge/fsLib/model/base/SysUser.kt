package com.fonrouge.fsLib.model.base

import com.fonrouge.fsLib.serializers.Id
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Serializable
@JsExport
data class SysUser(
    override val _id: Id<ISysUser>,
    override var name: String,
    override var rootUser: Boolean = false,
    override var password: String
) : ISysUser {
    companion object {
        var sysUsersCollectionName = "__sysUsers"
    }
}
