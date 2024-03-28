package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

abstract class ICommonContainer<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
    val itemKClass: KClass<T>,
    val idSerializer: KSerializer<ID>,
    // TODO: add here reference to ApiItem function and remove it from ConfigViewItem
    open val labelIdFunc: ((T?) -> String) = { t: T? -> t?.let { "${it._id}" } ?: "<no-item>" },
    open val labelItem: String = "${itemKClass.simpleName}",
    open val labelItemId: ((T?) -> String) = { t: T? -> "$labelItem: ${labelIdFunc(t)}" },
    open val labelList: String = "List of ${itemKClass.simpleName}",
    apiFilterSerializer: KSerializer<FILT>
) : ICommon<FILT>(
    apiFilterSerializer = apiFilterSerializer
) {
    @OptIn(InternalSerializationApi::class)
    val itemSerializer get() = itemKClass.serializer()

    @Suppress("unused")
    open fun validateItem(item: T, apiFilter: FILT = apiFilterInstance()): ItemState<T> {
        return ItemState(isOk = true)
    }
}
