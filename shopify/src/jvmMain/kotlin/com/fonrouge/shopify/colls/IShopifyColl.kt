package com.fonrouge.shopify.colls

import com.fonrouge.base.state.SimpleState
import com.fonrouge.base.types.OId
import com.fonrouge.fullStack.mongoDb.Coll
import com.fonrouge.shopify.model.*
import com.mongodb.client.model.UpdateOneModel
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

interface IShopifyColl<S : IStore<SID>, SID : OId<S>, ED : IElementsData<T>, T : Any> {
    companion object {
        val syncingElements: HashMap<OId<*>, Boolean> = HashMap()
        val httpClient = HttpClient(CIO) {
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
        private const val API_VERSION = "2025-07" // Use a stable Shopify Admin API version
    }

    private fun shopifyEndpoint(shopDomain: String) =
        "$shopDomain/admin/api/$API_VERSION/graphql.json".let {
            if (it.startsWith("https://")) it else "https://$it"
        }

    val elementKClass: KClass<ED>
    val elementQuery: String?
    val storeCollFun: () -> IStoreColl<*, S, SID, *, *>

    @OptIn(InternalSerializationApi::class)
    suspend fun graphQLRequest(
        store: IStore<*>,
        elementQuery: String,
        first: Int = 250,
        query: String? = null,
        after: String? = null
    ): GraphQLResponse<ED> {
        val request = GraphQLRequest(
            query = elementQuery,
            variables = ElementsVars(first = first, query = query, after = after)
        )
        val response = httpClient.post(urlString = shopifyEndpoint(store.url)) {
            header("X-Shopify-Access-Token", store.accessToken)
            header("Accept", "application/json")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return Json.decodeFromString(
            deserializer = GraphQLResponse.serializer(elementKClass.serializer()),
            string = response.bodyAsText()
        )
    }

    @Suppress("unused")
    suspend fun syncShopifyToLocal(
        storeId: SID,
    ): SimpleState {
        val elementQuery = elementQuery ?: return SimpleState(isOk = false, msgError = "No elementQuery defined")
        if (syncingElements[storeId] == true) return SimpleState(isOk = false, msgError = "Syncing products...")
        syncingElements[storeId] = true
        val store = storeCollFun().findById(storeId) ?: return SimpleState(isOk = false, msgError = "Store not found")
        var cursor: String? = null
        var hasNext = true
        while (hasNext) {
            val result: GraphQLResponse<ED> = graphQLRequest(
                store = store,
                elementQuery = elementQuery,
                first = 250,
                query = null,     // e.g., "status:active"
                after = cursor
            )

            // Handle GraphQL errors
            if (!result.errors.isNullOrEmpty()) {
                val messages = result.errors.joinToString("; ") { it.message }
                throw IllegalStateException("Shopify GraphQL errors: $messages")
            }

            val connection = result.data?.elements
                ?: break

            connection.edges.forEach { edge: ElementEdge<T> ->
                val x = edge.node
                println("x = $x")
//                titles += edge.node.title
            }

            hasNext = connection.pageInfo.hasNextPage
            cursor = connection.pageInfo.endCursor
        }
        syncingElements[storeId] = false
        return SimpleState(isOk = true)
    }
}
