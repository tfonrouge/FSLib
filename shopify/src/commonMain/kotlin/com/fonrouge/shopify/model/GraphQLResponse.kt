package com.fonrouge.shopify.model

import kotlinx.serialization.Serializable

@Serializable
data class GraphQLResponse<T>(
    val data: T? = null,
    val errors: List<GraphQLError>? = null,
    val extensions: Extensions? = null,
) {
    @Serializable
    data class Extensions(
        val cost: Cost,
    ) {
        @Serializable
        data class Cost(
            val requestedQueryCost: Int,
            val actualQueryCost: Int,
            val throttleStatus: ThrottleStatus,
        ) {
            @Serializable
            data class ThrottleStatus(
                val maximumAvailable: Double,
                val currentlyAvailable: Int,
                val restoreRate: Double
            )
        }
    }
}
