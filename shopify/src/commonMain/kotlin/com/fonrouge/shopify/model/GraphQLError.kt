package com.fonrouge.shopify.model

import kotlinx.serialization.Serializable

@Serializable
data class GraphQLError(
    val message: String
)
