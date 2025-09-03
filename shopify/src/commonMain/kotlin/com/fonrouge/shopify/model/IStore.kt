package com.fonrouge.shopify.model

import com.fonrouge.base.model.BaseDoc
import com.fonrouge.base.types.OId

interface IStore<ID : OId<*>> : BaseDoc<ID> {
    val name: String
    val url: String
    val apiVersion: String
    val accessToken: String
    val byteArrayImage: String?
    val enabled: Boolean
    val totalOnEachStore: Boolean
}
