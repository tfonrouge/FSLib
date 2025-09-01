package com.fonrouge.shopify.colls

import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.types.OId
import com.fonrouge.fullStack.mongoDb.Coll
import com.fonrouge.shopify.model.IStore

abstract class IStoreColl<CC : ICommonContainer<S, ID, FILT>, S : IStore<ID>, ID : OId<S>, FILT : IApiFilter<*>, UID : Any>(
    commonContainer: CC,
    debug: Boolean = false,
) : Coll<CC, S, ID, FILT, UID>(
    commonContainer = commonContainer,
    debug = debug,
)
