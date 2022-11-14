package com.fonrouge.fsLib.model.base

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Serializable
@JsExport
internal data class SysUser(
    override val _id: String,
    override var name: String,
    override var rootUser: Boolean = false,
    override var password: String
) : ISysUser {
    companion object {
        var sysUsersCollectionName = "__sysUsers"
    }
}
