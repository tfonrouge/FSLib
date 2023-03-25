package com.fonrouge.fsLib.model.base

import com.fonrouge.fsLib.serializers.OId

interface ISysUser : BaseDoc<OId<ISysUser>> {
    override val _id: OId<ISysUser>
    var name: String
    var rootUser: Boolean
    var password: String
}
