package com.fonrouge.fsLib.model.base

import com.fonrouge.fsLib.serializers.OId

interface IGroupUser : BaseDoc<OId<IGroupUser>> {
    override val _id: OId<IGroupUser>
    val description: String
}
