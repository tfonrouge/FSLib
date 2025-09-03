package com.fonrouge.shopify.model

import kotlinx.serialization.Serializable

@Serializable
data class ElementsVars(
    val first: Int,
    val query: String? = null,
    val after: String? = null
)
