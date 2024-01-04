package com.fonrouge.fsLib.model.base

import com.fonrouge.fsLib.serializers.OId

interface IGroupOfUser<T: Any> : BaseDoc<OId<T>> {
    override val _id: OId<T>
    val description: String
}
