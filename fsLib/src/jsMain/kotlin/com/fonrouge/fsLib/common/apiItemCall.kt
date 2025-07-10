package com.fonrouge.fsLib.common

import com.fonrouge.fsLib.commonServices.IApiCommonService
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.apiData.IApiItem
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import kotlin.js.Promise

/**
 * Executes an API item query to create a call to fetch the item's state and apply a transformation to the result.
 *
 * @param apiItemFun The suspend function provided by the API service that operates on the given API item and returns its state.
 * @param apiFilter The filter applied to the query to refine the results. Defaults to a new instance of the filter.
 * @param transform A function to transform the resulting item state into the desired return type.
 * @return A promise containing the transformed result of the item state.
 */
@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : IApiCommonService, R : Any?> CC.apiItemQueryCreateCall(
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    id: ID? = null,
    apiFilter: FILT = apiFilterInstance(),
    transform: ((ItemState<T>) -> R),
): Promise<R> = getItemState(
    apiItemFun = apiItemFun,
    apiItem = apiItemQueryCreate(id = id, apiFilter = apiFilter),
    transform = transform
)

/**
 * Makes a query to read a specific item from an API and processes the result using a transform function.
 *
 * @param apiItemFun A suspend function from the API service type `AIS` that accepts an `IApiItem`
 *                   representing the API query, returning an `ItemState` with the queried item.
 * @param id The identifier of the item to be queried.
 * @param apiFilter An optional filter of type `FILT` to refine the query; defaults to an instance of `FILT`.
 * @param transform A function to process the resulting `ItemState`, transforming it into a desired return type `R`.
 * @return A `Promise` wrapping the result of the transform function applied to the `ItemState` of the queried item.
 */
@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : IApiCommonService, R : Any?> CC.apiItemQueryReadCall(
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    id: ID,
    apiFilter: FILT = apiFilterInstance(),
    transform: ((ItemState<T>) -> R),
): Promise<R> = getItemState(
    apiItemFun = apiItemFun,
    apiItem = apiItemQueryRead(id, apiFilter),
    transform = transform
)

/**
 * Executes a query update call for a specific API item, applying the provided transformation to the resulting item state.
 *
 * @param apiItemFun A suspend function of the API service that processes the given API item and returns its state.
 * @param id The unique identifier of the item to be queried and updated.
 * @param apiFilter The API filter to be applied during the query. Defaults to an instance created by `apiFilterInstance()`.
 * @param transform A function to transform the resulting item state into the desired return type.
 * @return A `Promise` containing the transformed result of the query update call.
 */
@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : IApiCommonService, R : Any?> CC.apiItemQueryUpdateCall(
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    id: ID,
    apiFilter: FILT = apiFilterInstance(),
    transform: ((ItemState<T>) -> R),
): Promise<R> = getItemState(
    apiItemFun = apiItemFun,
    apiItem = apiItemQueryUpdate(id, apiFilter),
    transform = transform
)

/**
 * Executes a delete operation on a specific API item and processes the result with a transformation function.
 *
 * @param apiItemFun The suspend function provided by the API service to delete an item and return its state.
 * @param id The unique identifier of the item to be deleted.
 * @param apiFilter An API filter used to refine the query criteria. By default, it is initialized with `apiFilterInstance()`.
 * @param transform A transformation function that processes the resulting `ItemState` into the desired output type.
 * @return A `Promise` of the transformed result produced by the transformation function.
 */
@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : IApiCommonService, R : Any?> CC.apiItemQueryDeleteCall(
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    id: ID,
    apiFilter: FILT = apiFilterInstance(),
    transform: ((ItemState<T>) -> R),
): Promise<R> = getItemState(
    apiItemFun = apiItemFun,
    apiItem = apiItemQueryDelete(id, apiFilter),
    transform = transform
)

/**
 * Invokes a create action on an API item and transforms its resulting state.
 *
 * @param apiItemFun A suspend function provided by the API service that defines how the API item creation should be processed.
 * @param item The item to be created. Must extend `BaseDoc`.
 * @param apiFilter The API filter instance used for additional querying or filtering options. Defaults to `apiFilterInstance()`.
 * @param transform A function to transform the resulting `ItemState` of the created item into a desired output type.
 * @return A `Promise` of the transformed result, which is of type `R`.
 */
@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : IApiCommonService, R : Any?> CC.apiItemActionCreateCall(
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    item: T,
    apiFilter: FILT = apiFilterInstance(),
    transform: ((ItemState<T>) -> R),
): Promise<R> = getItemState(
    apiItemFun = apiItemFun,
    apiItem = apiItemActionCreate(item, apiFilter),
    transform = transform
)

/**
 * Executes an API call for updating an item within the context of a common container.
 *
 * @param apiItemFun A suspend function representing the API call for updating the item.
 *                   It takes an [IApiItem] containing the item and its associated filters
 *                   and returns an [ItemState] representing the result of the operation.
 * @param item The item of type [T] to be updated.
 * @param apiFilter The API filter of type [FILT] to be used in the operation.
 *                  Defaults to an instance created by `apiFilterInstance()`.
 * @param orig The original version of the item prior to the update, if applicable. Can be null.
 * @param transform A lambda function to transform the resulting [ItemState] into a different
 *                  type [R] if needed.
 *
 * @return A [Promise] wrapping the transformed result of type [R].
 */
@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : IApiCommonService, R : Any?> CC.apiItemActionUpdateCall(
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    item: T,
    apiFilter: FILT = apiFilterInstance(),
    orig: T?,
    transform: ((ItemState<T>) -> R),
): Promise<R> = getItemState(
    apiItemFun = apiItemFun,
    apiItem = apiItemActionUpdate(item, apiFilter, orig),
    transform = transform
)

/**
 * Deletes an item using an asynchronous API call and processes the result.
 *
 * @param apiItemFun A suspending function representing the API action to delete an item, which operates on the provided `IApiItem`.
 * @param item The item of type `T` to be deleted.
 * @param apiFilter The filter of type `FILT` to be used during the delete operation. Defaults to an instance of `FILT`.
 * @param transform A transformation function applied to the resulting `ItemState` of the deleted item.
 * @return A `Promise` of the transformed result of type `R`.
 */
@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : IApiCommonService, R : Any?> CC.apiItemActionDeleteCall(
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    item: T,
    apiFilter: FILT = apiFilterInstance(),
    transform: ((ItemState<T>) -> R),
): Promise<R> = getItemState(
    apiItemFun = apiItemFun,
    apiItem = apiItemActionDelete(item, apiFilter),
    transform = transform
)
