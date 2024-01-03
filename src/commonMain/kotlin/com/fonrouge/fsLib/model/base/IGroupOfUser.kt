package com.fonrouge.fsLib.model.base

import com.fonrouge.fsLib.serializers.OId

interface IGroupOfUser : BaseDoc<OId<IGroupOfUser>> {
    override val _id: OId<IGroupOfUser>
    val description: String
}
