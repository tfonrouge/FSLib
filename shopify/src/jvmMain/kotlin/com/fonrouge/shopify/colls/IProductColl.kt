package com.fonrouge.shopify.colls

import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.state.SimpleState
import com.fonrouge.base.types.OId
import com.fonrouge.base.types.StringId
import com.fonrouge.fullStack.mongoDb.Coll
import com.fonrouge.shopify.common.ICommonProduct
import com.fonrouge.shopify.model.IProduct
import com.fonrouge.shopify.model.IStore

abstract class IProductColl<CC : ICommonProduct<P, ID, FILT>, P : IProduct<ID>, ID : StringId<P>, FILT : IApiFilter<*>, UID : Any, S : IStore<SID>, SID : OId<S>>(
    commonContainer: CC,
    debug: Boolean = false,
) : Coll<CC, P, ID, FILT, UID>(
    commonContainer = commonContainer,
    debug = debug,
) {
    companion object {
        val syncingProducts: MutableMap<OId<*>, Boolean> = mutableMapOf()
    }

    open val storeColl: () -> IStoreColl<*, S, SID, *, *> = { throw NotImplementedError() }

    suspend fun syncShopifyToLocal(storeId: SID): SimpleState {
        if (syncingProducts[storeId] == true) {
            return SimpleState(isOk = false, msgError = "Syncing products...")
        }
        syncingProducts[storeId] = true

        syncingProducts[storeId] = false
        return SimpleState(isOk = true)
    }
}
