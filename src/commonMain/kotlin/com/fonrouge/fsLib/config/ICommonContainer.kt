package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.CrudTask
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.apiData.serializeMasterItemId
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

abstract class ICommonContainer<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
    val itemKClass: KClass<T>,
    val idSerializer: KSerializer<ID>,
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
    fun apiItem(
        id: ID? = null,
        item: T? = null,
        callType: ApiItem.CallType = ApiItem.CallType.Query,
        crudTask: CrudTask = CrudTask.Read,
        apiFilter: FILT = apiFilterInstance(),
    ): ApiItem<T, ID, FILT> {
        return ApiItem(
            id = id,
            item = item,
            callType = callType,
            crudTask = crudTask,
            apiFilter = apiFilter
        )
    }

    @Suppress("unused")
    inline fun <MIT : BaseDoc<MID>, reified MID : Any> apiItem(
        id: ID? = null,
        item: T? = null,
        callType: ApiItem.CallType = ApiItem.CallType.Query,
        crudTask: CrudTask = CrudTask.Read,
        apiFilter: FILT = apiFilterInstance(),
        masterItem: MIT?,
    ): ApiItem<T, ID, FILT> {
        return ApiItem(
            id = id,
            item = item,
            callType = callType,
            crudTask = crudTask,
            apiFilter = apiFilter.serializeMasterItemId(masterItem?._id)
        )
    }

    @Suppress("unused")
    open fun validateItem(item: T, apiFilter: FILT = apiFilterInstance()): ItemState<T> {
        return ItemState(isOk = true)
    }
}
