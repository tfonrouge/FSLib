package com.fonrouge.shopify.common

import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.types.StringId
import com.fonrouge.shopify.model.IProduct
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

@OptIn(InternalSerializationApi::class)
abstract class ICommonProduct<P : IProduct, FILT : IApiFilter<*>>(
    itemKClass: KClass<P>,
    apiFilterSerializer: KSerializer<FILT>,
) : ICommonContainer<P, StringId<IProduct>, FILT>(
    itemKClass = itemKClass,
    idSerializer = StringId.serializer(serializer()),
    apiFilterSerializer = apiFilterSerializer,
    labelItem = "Shopify Product",
    labelList = "Shopify Products"
)
