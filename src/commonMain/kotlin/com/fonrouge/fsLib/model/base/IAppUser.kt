package com.fonrouge.fsLib.model.base

import kotlin.js.JsExport

@JsExport
interface IAppUser : BaseModel<String> {
    override val _id: String
    var code: String
    var name: String
    var rootUser: Boolean
    var password: String
}
