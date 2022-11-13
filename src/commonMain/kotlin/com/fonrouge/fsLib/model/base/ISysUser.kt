package com.fonrouge.fsLib.model.base

import kotlin.js.JsExport

var iSysUsersCollectionName = "__sysUsers"

@JsExport
interface ISysUser : BaseModel<String> {
    override val _id: String
    var name: String
    var rootUser: Boolean
    var password: String
}
