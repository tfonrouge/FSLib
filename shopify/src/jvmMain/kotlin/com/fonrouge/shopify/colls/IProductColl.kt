package com.fonrouge.shopify.colls

import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.types.LongId
import com.fonrouge.fullStack.mongoDb.Coll
import com.fonrouge.shopify.common.ICommonProduct
import com.fonrouge.shopify.model.IProduct

abstract class IProductColl<CC : ICommonProduct<P, ID, FILT>, P : IProduct<ID>, ID : LongId<P>, FILT : IApiFilter<*>, UID : Any>(
    commonContainer: CC,
    debug: Boolean = false,
) : Coll<CC, P, ID, FILT, UID>(
    commonContainer = commonContainer,
    debug = debug,
)
