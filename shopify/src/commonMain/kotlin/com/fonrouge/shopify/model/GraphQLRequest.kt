package com.fonrouge.shopify.model

import kotlinx.serialization.Serializable

@Serializable
data class GraphQLRequest<V>(
    val query: String,
    val variables: V
)
