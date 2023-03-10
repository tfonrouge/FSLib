package com.fonrouge.fsLib.model.base

import com.fonrouge.fsLib.serializers.Id
import kotlin.js.JsExport

@JsExport
interface ISysUser : BaseModel<Id<ISysUser>> {
    override val _id: Id<ISysUser>
    var name: String
    var rootUser: Boolean
    var password: String
}
