package com.fonrouge.shopify.model

import kotlinx.serialization.Serializable

@Serializable
data class PageInfo(
    val hasNextPage: Boolean,
    val endCursor: String? = null
)
