package com.fonrouge.shopify.colls

import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.types.OId
import com.fonrouge.fullStack.mongoDb.Coll
import com.fonrouge.fullStack.mongoDb.collation
import com.fonrouge.shopify.model.IStore
import com.mongodb.client.model.IndexOptions
import org.litote.kmongo.coroutine.CoroutineCollection

abstract class IStoreColl<CC : ICommonContainer<S, ID, FILT>, S : IStore<ID>, ID : OId<S>, FILT : IApiFilter<*>, UID : Any>(
    commonContainer: CC,
    debug: Boolean = false,
) : Coll<CC, S, ID, FILT, UID>(
    commonContainer = commonContainer,
    debug = debug,
) {
    override suspend fun CoroutineCollection<S>.indexes() {
        ensureUniqueIndex(IStore<*>::name, indexOptions = IndexOptions().collation(collation("en_US")))
    }
}
