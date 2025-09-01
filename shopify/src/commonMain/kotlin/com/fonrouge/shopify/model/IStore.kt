package com.fonrouge.shopify.model

import com.fonrouge.base.model.BaseDoc

interface IStore<ID> : BaseDoc<ID> {
    val name: String
    val url: String
    val usernamePassword: String
    val byteArrayImage: String?
    val enabled: Boolean
    val totalOnEachStore: Boolean
}
