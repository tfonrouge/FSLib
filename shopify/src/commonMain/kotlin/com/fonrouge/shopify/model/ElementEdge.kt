package com.fonrouge.shopify.model

import kotlinx.serialization.Serializable

@Serializable
data class ElementEdge<T>(
    val cursor: String,
    val node: T
)
