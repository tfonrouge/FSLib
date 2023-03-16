package com.fonrouge.fsLib.model.base

import com.fonrouge.fsLib.serializers.OId
import kotlin.js.JsExport

@JsExport
interface ISysUser : BaseModel<OId<ISysUser>> {
    override val _id: OId<ISysUser>
    var name: String
    var rootUser: Boolean
    var password: String
}
