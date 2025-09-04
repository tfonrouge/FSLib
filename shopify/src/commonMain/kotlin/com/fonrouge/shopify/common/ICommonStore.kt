package com.fonrouge.shopify.common

import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.types.OId
import com.fonrouge.shopify.model.IStore
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

@OptIn(InternalSerializationApi::class)
abstract class ICommonStore<S : IStore, FILT : IApiFilter<*>>(
    itemKClass: KClass<S>,
    apiFilterSerializer: KSerializer<FILT>,
) : ICommonContainer<S, OId<IStore>, FILT>(
    itemKClass = itemKClass,
    idSerializer = OId.serializer(serializer()),
    apiFilterSerializer = apiFilterSerializer,
    labelItem = "Shopify Store",
    labelList = "Shopify Stores"
)
