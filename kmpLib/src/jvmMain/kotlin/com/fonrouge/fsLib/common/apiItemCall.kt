package com.fonrouge.fsLib.common

import com.fonrouge.fsLib.commonServices.IApiCommonService
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.apiData.IApiItem
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import kotlinx.serialization.json.Json

@Suppress("unused")
suspend fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, SRV : IApiCommonService> CC.apiItemQueryCreateCall(
    service: SRV,
    apiItemFun: suspend SRV.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    apiFilter: FILT = apiFilterInstance(),
): ItemState<T> = apiItemFun(
    service,
    IApiItem.Upsert.Create.Query(
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter)
    )
)

@Suppress("unused")
suspend fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, SRV : IApiCommonService> CC.apiItemQueryReadCall(
    service: SRV,
    apiItemFun: suspend SRV.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    id: ID,
    apiFilter: FILT = apiFilterInstance(),
): ItemState<T> = apiItemFun(
    service,
    IApiItem.Read(
        serializedId = Json.encodeToString(idSerializer, id),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter)
    )
)

@Suppress("unused")
suspend fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, SRV : IApiCommonService> CC.apiItemQueryUpdateCall(
    service: SRV,
    apiItemFun: suspend SRV.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    id: ID,
    apiFilter: FILT = apiFilterInstance(),
): ItemState<T> = apiItemFun(
    service,
    IApiItem.Upsert.Update.Query(
        serializedId = Json.encodeToString(idSerializer, id),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter)
    )
)

@Suppress("unused")
suspend fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, SRV : IApiCommonService> CC.apiItemQueryDeleteCall(
    service: SRV,
    apiItemFun: suspend SRV.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    id: ID,
    apiFilter: FILT = apiFilterInstance(),
): ItemState<T> = apiItemFun(
    service,
    IApiItem.Delete.Query(
        serializedId = Json.encodeToString(idSerializer, id),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter)
    )
)

@Suppress("unused")
suspend fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, SRV : IApiCommonService> CC.apiItemActionCreateCall(
    service: SRV,
    apiItemFun: suspend SRV.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    item: T,
    apiFilter: FILT = apiFilterInstance(),
): ItemState<T> = apiItemFun(
    service,
    IApiItem.Upsert.Create.Action(
        serializedItem = Json.encodeToString(itemSerializer, item),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter)
    )
)

@Suppress("unused")
suspend fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, SRV : IApiCommonService> CC.apiItemActionUpdateCall(
    service: SRV,
    apiItemFun: suspend SRV.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    item: T,
    apiFilter: FILT = apiFilterInstance(),
    orig: T?,
): ItemState<T> = apiItemFun(
    service,
    IApiItem.Upsert.Update.Action(
        serializedItem = Json.encodeToString(itemSerializer, item),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter),
        serializedOrig = orig?.let { Json.encodeToString(itemSerializer, orig) }
    )
)

@Suppress("unused")
suspend fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, SRV : IApiCommonService> CC.apiItemActionDeleteCall(
    service: SRV,
    apiItemFun: suspend SRV.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    item: T,
    apiFilter: FILT = apiFilterInstance(),
): ItemState<T> = apiItemFun(
    service,
    IApiItem.Delete.Action(
        serializedItem = Json.encodeToString(itemSerializer, item),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter)
    )
)
