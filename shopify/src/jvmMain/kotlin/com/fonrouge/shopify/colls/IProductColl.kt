package com.fonrouge.shopify.colls

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.http.HttpHeader
import com.fonrouge.GetProductDetailQuery
import com.fonrouge.GetProductsQuery
import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.types.StringId
import com.fonrouge.shopify.common.ICommonProduct
import com.fonrouge.shopify.model.ElementConnection
import com.fonrouge.shopify.model.IElementsData
import com.fonrouge.shopify.model.IProduct
import com.fonrouge.shopify.model.IStore
import kotlinx.serialization.*

abstract class IProductColl<CC : ICommonProduct<T, FILT>, T : IProduct, FILT : IApiFilter<*>, UID : Any, S : IStore>(
    commonContainer: CC,
    debug: Boolean = false,
) : IShopifyColl<CC, T, StringId<IProduct>, FILT, UID, S, IProductColl.ProductsData<T>>(
    commonContainer = commonContainer,
    debug = debug,
) {
    @Serializable
    data class ProductsData<T : IProduct>(
        @SerialName("products")
        override val elements: ElementConnection<T>
    ) : IElementsData<T>

    final override val elementQuery: String by lazy {
        $$"""
    query Products($first: Int!, $query: String, $after: String) {
      products(first: $first, query: $query, after: $after) {
        pageInfo {
          hasNextPage
          endCursor
        }
        edges {
          cursor
          node {
          $${itemProperties()}
          }
        }
      }
    }
""".trimIndent()
    }

    @OptIn(InternalSerializationApi::class)
    final override val elementSerializer: KSerializer<ProductsData<T>> =
        ProductsData.serializer(commonContainer.itemKClass.serializer())

    val apolloClient
        get() = ApolloClient.Builder()
            .serverUrl("https://azucardulcerias.myshopify.com/admin/api/2025-07/graphql.json")
            .httpHeaders(
                listOf(
                    HttpHeader("X-Shopify-Access-Token", "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")
                )
            )
            .build()

    suspend fun fetchProducts() {
        val r = apolloClient.query(GetProductsQuery(100)).execute()
        println("r = $r")
        val response = apolloClient.query(GetProductDetailQuery("gid://shopify/Product/1577852469350")).execute()
        if (response.data != null) {
            val x = response.data?.product
            println("x = $x")
//            response.data?.products?.edges?.forEach { edge: AllProductsQuery.Edge ->
//                println("handle = ${edge.node.handle} title = ${edge.node.title}")
//            }
        } else {
            println("response.errors = ${response.errors}")
        }
    }
}
