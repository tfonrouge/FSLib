package com.fonrouge.shopify.common

import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.types.StringId
import com.fonrouge.shopify.model.IProduct
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

abstract class ICommonProduct<P : IProduct<ID>, ID : StringId<P>, FILT : IApiFilter<*>>(
    itemKClass: KClass<P>,
    idSerializer: KSerializer<ID>,
    apiFilterSerializer: KSerializer<FILT>,
) : ICommonContainer<P, ID, FILT>(
    itemKClass = itemKClass,
    idSerializer = idSerializer,
    apiFilterSerializer = apiFilterSerializer,
    labelItem = "Shopify Product",
    labelList = "Shopify Products"
)
