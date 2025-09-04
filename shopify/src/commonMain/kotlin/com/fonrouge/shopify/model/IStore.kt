package com.fonrouge.shopify.model

import com.fonrouge.base.model.BaseDoc
import com.fonrouge.base.types.OId
import kotlinx.serialization.Serializable

@Serializable
abstract class IStore : BaseDoc<OId<IStore>> {
    abstract val name: String
    abstract val url: String
    abstract val apiVersion: String
    abstract val accessToken: String
    abstract val byteArrayImage: String?
    abstract val enabled: Boolean
    abstract val totalOnEachStore: Boolean
}
