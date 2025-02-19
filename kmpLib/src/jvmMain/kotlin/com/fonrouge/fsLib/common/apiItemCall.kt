package com.fonrouge.fsLib.common

import com.fonrouge.fsLib.commonServices.IApiCommonService
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.apiData.IApiItem
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import kotlinx.serialization.json.Json

@Suppress("unused")
suspend fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : IApiCommonService> CC.apiItemQueryCreateCall(
    serviceManager: AIS,
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    apiFilter: FILT = apiFilterInstance(),
): ItemState<T> = apiItemFun(
    serviceManager,
    IApiItem.Upsert.Create.Query(
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter)
    )
)

@Suppress("unused")
suspend fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : IApiCommonService> CC.apiItemQueryReadCall(
    serviceManager: AIS,
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    id: ID,
    apiFilter: FILT = apiFilterInstance(),
): ItemState<T> = apiItemFun(
    serviceManager,
    IApiItem.Read(
        serializedId = Json.encodeToString(idSerializer, id),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter)
    )
)

@Suppress("unused")
suspend fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : IApiCommonService> CC.apiItemQueryUpdateCall(
    serviceManager: AIS,
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    id: ID,
    apiFilter: FILT = apiFilterInstance(),
): ItemState<T> = apiItemFun(
    serviceManager,
    IApiItem.Upsert.Update.Query(
        serializedId = Json.encodeToString(idSerializer, id),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter)
    )
)

@Suppress("unused")
suspend fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : IApiCommonService> CC.apiItemQueryDeleteCall(
    serviceManager: AIS,
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    id: ID,
    apiFilter: FILT = apiFilterInstance(),
): ItemState<T> = apiItemFun(
    serviceManager,
    IApiItem.Delete.Query(
        serializedId = Json.encodeToString(idSerializer, id),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter)
    )
)

@Suppress("unused")
suspend fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : IApiCommonService> CC.apiItemActionCreateCall(
    serviceManager: AIS,
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    item: T,
    apiFilter: FILT = apiFilterInstance(),
): ItemState<T> = apiItemFun(
    serviceManager,
    IApiItem.Upsert.Create.Action(
        serializedItem = Json.encodeToString(itemSerializer, item),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter)
    )
)

@Suppress("unused")
suspend fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : IApiCommonService> CC.apiItemActionUpdateCall(
    serviceManager: AIS,
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    item: T,
    apiFilter: FILT = apiFilterInstance(),
    orig: T?,
): ItemState<T> = apiItemFun(
    serviceManager,
    IApiItem.Upsert.Update.Action(
        serializedItem = Json.encodeToString(itemSerializer, item),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter),
        serializedOrig = orig?.let { Json.encodeToString(itemSerializer, orig) }
    )
)

@Suppress("unused")
suspend fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : IApiCommonService> CC.apiItemActionDeleteCall(
    serviceManager: AIS,
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    item: T,
    apiFilter: FILT = apiFilterInstance(),
): ItemState<T> = apiItemFun(
    serviceManager,
    IApiItem.Delete.Action(
        serializedItem = Json.encodeToString(itemSerializer, item),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter)
    )
)
