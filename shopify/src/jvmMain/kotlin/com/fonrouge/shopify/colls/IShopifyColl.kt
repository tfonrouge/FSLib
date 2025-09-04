package com.fonrouge.shopify.colls

import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.model.BaseDoc
import com.fonrouge.base.state.SimpleState
import com.fonrouge.base.types.OId
import com.fonrouge.fullStack.mongoDb.Coll
import com.fonrouge.fullStack.mongoDb.toBsonSet
import com.fonrouge.shopify.model.*
import com.mongodb.client.model.UpdateOneModel
import com.mongodb.client.model.UpdateOptions
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import org.litote.kmongo.eq
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

abstract class IShopifyColl<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, UID : Any, S : IStore, ED : IElementsData<T>>(
    commonContainer: CC,
    debug: Boolean = false,
) : Coll<CC, T, ID, FILT, UID>(
    commonContainer = commonContainer,
    debug = debug,
) {
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

    protected fun itemProperties(): String =
        commonContainer.itemKClass.memberProperties.joinToString("\n") {
            it.findAnnotation<SerialName>()?.value ?: it.name
        }

    private fun shopifyEndpoint(shopDomain: String) =
        "$shopDomain/admin/api/$API_VERSION/graphql.json".let {
            if (it.startsWith("https://")) it else "https://$it"
        }

    abstract val elementQuery: String?
    abstract val elementSerializer: KSerializer<ED>
    abstract val storeCollFun: () -> IStoreColl<*, S, *, *>

    @OptIn(InternalSerializationApi::class)
    suspend fun graphQLRequest(
        store: IStore,
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
            deserializer = GraphQLResponse.serializer(elementSerializer),
            string = response.bodyAsText()
        )
    }

    @Suppress("unused")
    suspend fun syncShopifyToLocal(
        storeId: OId<IStore>,
    ): SimpleState {
        val elementQuery = elementQuery ?: return SimpleState(isOk = false, msgError = "No elementQuery defined")
        if (syncingElements[storeId] == true) return SimpleState(isOk = false, msgError = "Syncing products...")
        syncingElements[storeId] = true
        val store = storeCollFun().findById(storeId) ?: return SimpleState(isOk = false, msgError = "Store not found")
        var cursor: String? = null
        var hasNext = true
        val updateOneModels = mutableListOf<UpdateOneModel<T>>()
        while (hasNext) {
            val result: GraphQLResponse<ED> = graphQLRequest(
                store = store,
                elementQuery = elementQuery,
                first = 250,
                query = null,     // e.g., "status:active"
                after = cursor
            )
            if (!result.errors.isNullOrEmpty()) {
                val messages = result.errors.joinToString("; ") { it.message }
                throw IllegalStateException("Shopify GraphQL errors: $messages")
            }
            val connection = result.data?.elements ?: break
            connection.edges.forEach { edge: ElementEdge<T> ->
                val t: T = edge.node
                updateOneModels += UpdateOneModel(
                    BaseDoc<*>::_id eq t._id,
                    toBsonSet(t),
                    UpdateOptions().upsert(true)
                )
                println("x = $t")
            }
            hasNext = connection.pageInfo.hasNextPage
            cursor = connection.pageInfo.endCursor
        }
        if (updateOneModels.isNotEmpty()) {
            val r = coroutine.bulkWrite(updateOneModels)
            println("bulkWrite result = $r")
        }
        syncingElements[storeId] = false
        return SimpleState(isOk = true)
    }
}
