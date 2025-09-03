package com.fonrouge.shopify.model

import com.fonrouge.base.model.BaseDoc
import com.fonrouge.base.types.StringId

interface IProduct<ID : StringId<*>> : BaseDoc<ID> {
    val title: String
    val handle: String?
    val vendor: String?
}
