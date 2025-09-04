package com.fonrouge.shopify.colls

import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.types.OId
import com.fonrouge.fullStack.mongoDb.collation
import com.fonrouge.shopify.model.ElementConnection
import com.fonrouge.shopify.model.IElementsData
import com.fonrouge.shopify.model.IStore
import com.mongodb.client.model.IndexOptions
import kotlinx.serialization.*
import org.litote.kmongo.coroutine.CoroutineCollection

abstract class IStoreColl<CC : ICommonContainer<T, OId<IStore>, FILT>, T : IStore, FILT : IApiFilter<*>, UID : Any>(
    commonContainer: CC,
    debug: Boolean = false,
) : IShopifyColl<CC, T, OId<IStore>, FILT, UID, T, IStoreColl.StoresData<T>>(
    commonContainer = commonContainer,
    debug = debug,
) {
    @Serializable
    data class StoresData<T : IStore>(
        @SerialName("products")
        override val elements: ElementConnection<T>
    ) : IElementsData<T>

    @OptIn(InternalSerializationApi::class)
    override val elementSerializer: KSerializer<StoresData<T>> =
        StoresData.serializer(commonContainer.itemKClass.serializer())

    override suspend fun CoroutineCollection<T>.indexes() {
        ensureUniqueIndex(IStore::name, indexOptions = IndexOptions().collation(collation("en_US")))
    }
}
