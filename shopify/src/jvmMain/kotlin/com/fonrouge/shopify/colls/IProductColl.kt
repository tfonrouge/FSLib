package com.fonrouge.shopify.colls

import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.types.OId
import com.fonrouge.base.types.StringId
import com.fonrouge.fullStack.mongoDb.Coll
import com.fonrouge.shopify.common.ICommonProduct
import com.fonrouge.shopify.model.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

abstract class IProductColl<CC : ICommonProduct<P, ID, FILT>, P : IProduct<ID>, ID : StringId<P>, FILT : IApiFilter<*>, UID : Any, S : IStore<SID>, SID : OId<S>>(
    commonContainer: CC,
    debug: Boolean = false,
) : IShopifyColl<S, SID, IProductColl.ProductsData>, Coll<CC, P, ID, FILT, UID>(
    commonContainer = commonContainer,
    debug = debug,
) {
    @Serializable
    data class ProductsData(
        @SerialName("products")
        override val elements: ElementConnection<Product>
    ) : IElementsData<Product>
}
