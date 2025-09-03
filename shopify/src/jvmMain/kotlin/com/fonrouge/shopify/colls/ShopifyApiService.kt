package com.fonrouge.shopify.colls

import com.fonrouge.base.types.StringId
import com.fonrouge.shopify.colls.IProductColl.Companion.syncingProducts
import com.fonrouge.shopify.model.IProduct
import com.fonrouge.shopify.model.IStore
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ShopifyApiService {
    @Serializable
    data class GraphQLRequest<V>(
        val query: String,
        val variables: V
    )

    @Serializable
    data class GraphQLError(
        val message: String
    )

    @Serializable
    data class GraphQLResponse<T>(
        val data: T? = null,
        val errors: List<GraphQLError>? = null
    )

    @Serializable
    data class ProductsVars(
        val first: Int,
        val query: String? = null,
        val after: String? = null
    )

    @Serializable
    data class Product(
        @SerialName("id")
        override val _id: StringId<Product>,
        override val title: String,
        override val handle: String? = null,
        override val vendor: String? = null
    ) : IProduct<StringId<Product>>

    @Serializable
    data class ObjectEdge<T>(
        val cursor: String,
        val node: T
    )

    // 3) Response models matching the selection set
    @Serializable
    data class PageInfo(
        val hasNextPage: Boolean,
        val endCursor: String? = null
    )

    @Serializable
    data class ObjectConnection<T>(
        val edges: List<ObjectEdge<T>>,
        val pageInfo: PageInfo
    )

    @Serializable
    data class ObjectsData<T>(
        val objects: ObjectConnection<T>
    )

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                }
            )
        }
    }
    private val API_VERSION = "2024-07" // Use a stable Shopify Admin API version
    private fun shopifyEndpoint(shopDomain: String) =
        "$shopDomain/admin/api/$API_VERSION/graphql.json".let {
            if (it.startsWith("https://")) it else "https://$it"
        }

    private val productsQuery = $$"""
    query Products($first: Int!, $query: String, $after: String) {
      products(first: $first, query: $query, after: $after) {
        pageInfo {
          hasNextPage
          endCursor
        }
        edges {
          cursor
          node {
            id
            title
            handle
            vendor
          }
        }
      }
    }
""".trimIndent()

    private suspend inline fun <reified T> graphQLRequest(
        store: IStore<*>,
        first: Int = 250,
        query: String? = null,
        after: String? = null
    ): GraphQLResponse<ObjectsData<T>> {
        val request = GraphQLRequest(
            query = productsQuery,
            variables = ProductsVars(first = first, query = query, after = after)
        )
        val response = httpClient.post(urlString = shopifyEndpoint(store.url)) {
            header("X-Shopify-Access-Token", store.accessToken)
            header("Accept", "application/json")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.body()
    }

    suspend fun listAllProductTitles2(
        store: IStore<*>,
    ): List<String> {

        if (syncingProducts[store._id] == true) return emptyList()
        syncingProducts[store._id] = true

        val titles = mutableListOf<String>()
        var cursor: String? = null
        var hasNext = true

        while (hasNext) {
            val result: GraphQLResponse<ObjectsData<Product>> = graphQLRequest<Product>(
                store = store,
                first = 50,
                query = null,     // e.g., "status:active"
                after = cursor
            )

            // Handle GraphQL errors
            if (!result.errors.isNullOrEmpty()) {
                val messages = result.errors.joinToString("; ") { it.message }
                throw IllegalStateException("Shopify GraphQL errors: $messages")
            }

            val connection = result.data?.objects
                ?: break

            connection.edges.forEach { edge ->
                titles += edge.node.title
            }

            hasNext = connection.pageInfo.hasNextPage
            cursor = connection.pageInfo.endCursor
        }
        syncingProducts[store._id] = false
        return titles
    }
}
