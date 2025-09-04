package com.fonrouge.shopify.colls

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
}
