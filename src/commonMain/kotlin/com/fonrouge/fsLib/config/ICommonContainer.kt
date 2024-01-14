package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

abstract class ICommonContainer<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
    val itemKClass: KClass<T>,
    val idSerializer: KSerializer<ID>,
    val labelIdFunc: ((T?) -> String)? = { it?._id?.toString() ?: "<no-item>" },
    val labelItem: String = "${itemKClass.simpleName}",
    val labelList: String = "List of ${itemKClass.simpleName}",
    apiFilterSerializer: KSerializer<FILT>
) : ICommon<FILT>(
    apiFilterSerializer = apiFilterSerializer
) {
    @OptIn(InternalSerializationApi::class)
    val itemSerializer get() = itemKClass.serializer()
    fun route(type: Type): String = "${type}${super.route()}"
    enum class Type {
        Item,
        List,
    }
}
