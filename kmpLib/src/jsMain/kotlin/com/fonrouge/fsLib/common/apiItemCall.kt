package com.fonrouge.fsLib.common

import com.fonrouge.fsLib.commonServices.IApiCommonService
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.apiData.IApiItem
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import io.kvision.remote.KVServiceManager
import kotlin.js.Promise

@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : IApiCommonService, R : Any?> CC.apiItemQueryCreateCall(
    serviceManager: KVServiceManager<AIS>,
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    apiFilter: FILT = apiFilterInstance(),
    transform: ((ItemState<T>) -> R)
): Promise<R> = getItemState(
    serviceManager = serviceManager,
    apiItemFun = apiItemFun,
    apiItem = apiItemQueryCreate(apiFilter),
    transform = transform
)

@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : IApiCommonService, R : Any?> CC.apiItemQueryReadCall(
    serviceManager: KVServiceManager<AIS>,
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    id: ID,
    apiFilter: FILT = apiFilterInstance(),
    transform: ((ItemState<T>) -> R)
): Promise<R> = getItemState(
    serviceManager = serviceManager,
    apiItemFun = apiItemFun,
    apiItem = apiItemQueryRead(id, apiFilter),
    transform = transform
)

@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : IApiCommonService, R : Any?> CC.apiItemQueryUpdateCall(
    serviceManager: KVServiceManager<AIS>,
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    id: ID,
    apiFilter: FILT = apiFilterInstance(),
    transform: ((ItemState<T>) -> R)
): Promise<R> = getItemState(
    serviceManager = serviceManager,
    apiItemFun = apiItemFun,
    apiItem = apiItemQueryUpdate(id, apiFilter),
    transform = transform
)

@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : IApiCommonService, R : Any?> CC.apiItemQueryDeleteCall(
    serviceManager: KVServiceManager<AIS>,
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    id: ID,
    apiFilter: FILT = apiFilterInstance(),
    transform: ((ItemState<T>) -> R)
): Promise<R> = getItemState(
    serviceManager = serviceManager,
    apiItemFun = apiItemFun,
    apiItem = apiItemQueryDelete(id, apiFilter),
    transform = transform
)

@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : IApiCommonService, R : Any?> CC.apiItemActionCreateCall(
    serviceManager: KVServiceManager<AIS>,
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    item: T,
    apiFilter: FILT = apiFilterInstance(),
    transform: ((ItemState<T>) -> R)
): Promise<R> = getItemState(
    serviceManager = serviceManager,
    apiItemFun = apiItemFun,
    apiItem = apiItemActionCreate(item, apiFilter),
    transform = transform
)

@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : IApiCommonService, R : Any?> CC.apiItemActionUpdateCall(
    serviceManager: KVServiceManager<AIS>,
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    item: T,
    apiFilter: FILT = apiFilterInstance(),
    orig: T?,
    transform: ((ItemState<T>) -> R)
): Promise<R> = getItemState(
    serviceManager = serviceManager,
    apiItemFun = apiItemFun,
    apiItem = apiItemActionUpdate(item, apiFilter, orig),
    transform = transform
)

@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : IApiCommonService, R : Any?> CC.apiItemActionDeleteCall(
    serviceManager: KVServiceManager<AIS>,
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    item: T,
    apiFilter: FILT = apiFilterInstance(),
    transform: ((ItemState<T>) -> R)
): Promise<R> = getItemState(
    serviceManager = serviceManager,
    apiItemFun = apiItemFun,
    apiItem = apiItemActionDelete(item, apiFilter),
    transform = transform
)
