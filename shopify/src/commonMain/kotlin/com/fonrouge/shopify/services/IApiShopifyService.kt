package com.fonrouge.shopify.services

import com.fonrouge.base.api.ApiList
import com.fonrouge.base.state.ListState
import com.fonrouge.shopify.model.IProduct
import com.fonrouge.shopify.model.IProductFilter

interface IApiShopifyService {
    suspend fun <P : IProduct<*>, FILT : IProductFilter<*, *>> apiGetProducts(apiList: ApiList<FILT>): ListState<P> {
        throw Exception("Not yet implemented")
    }
}
