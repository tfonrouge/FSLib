package com.fonrouge.shopify.common

import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.types.OId
import com.fonrouge.shopify.model.IStore
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

@OptIn(InternalSerializationApi::class)
abstract class ICommonStore<S : IStore<ID>, ID : OId<S>, FILT : IApiFilter<*>>(
    itemKClass: KClass<S>,
    idSerializer: KSerializer<ID>,
    apiFilterSerializer: KSerializer<FILT>,
) : ICommonContainer<S, ID, FILT>(
    itemKClass = itemKClass,
    idSerializer = idSerializer,
    apiFilterSerializer = apiFilterSerializer,
    labelItem = "Shopify Store",
    labelList = "Shopify Stores"
)
