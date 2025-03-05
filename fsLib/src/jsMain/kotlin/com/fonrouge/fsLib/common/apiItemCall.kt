package com.fonrouge.fsLib.common

import com.fonrouge.fsLib.commonServices.IApiCommonService
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.apiData.IApiItem
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import io.kvision.remote.KVServiceManager
import kotlin.js.Promise

/**
 * Creates an API item query call for creating a new item, executes the specified service function,
 * and returns the result in a transformed form.
 *
 * @param serviceManager The service manager that provides access to the API service.
 * @param apiItemFun The service function to handle the API item operation. The function receives an API item and returns an ItemState.
 * @param apiFilter The filter used to create the query. Defaults to a new instance of the filter.
 * @param transform A transformation function to process the resulting ItemState.
 * @return A promise of the transformation result based on the executed API item operation.
 */
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

/**
 * Performs a read operation on an API item based on the specified ID and filter,
 * and transforms the resulting item state to a desired output format.
 *
 * @param CC The type of the common container, which manages API items.
 * @param T The type of the items in the container, which must extend BaseDoc.
 * @param ID The type of the ID associated with the items, which must be a non-nullable type.
 * @param FILT The type of the API filter used for querying, which must extend IApiFilter.
 * @param AIS The type of the API service interface.
 * @param R The type of the transformed result returned by the function.
 * @param serviceManager The service manager responsible for managing the API service calls.
 * @param apiItemFun A suspend function representing the API method that handles the read operation.
 * @param id The ID of the item to be read.
 * @param apiFilter The filter used for querying. A default instance of the filter is used if not provided.
 * @param transform A function to transform the resulting ItemState to the desired return type.
 * @return A promise containing the transformed result of the read operation.
 */
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

/**
 * Executes an API item update query in a given service manager and transforms the result.
 *
 * @param serviceManager The service manager that provides the API services needed for the query.
 * @param apiItemFun A suspend function representing the API operation to be performed for the item.
 * @param id The identifier of the item to be updated.
 * @param apiFilter An optional filter to narrow down the query. A default filter instance is used if not provided.
 * @param transform A function to transform the resulting item state into the desired type.
 * @return A promise of the result produced by applying the transform function to the item state.
 */
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

/**
 * Executes a query to delete an API item and transforms the result.
 *
 * @param serviceManager The service manager instance responsible for handling `IApiCommonService` implementations.
 * @param apiItemFun The suspend function to execute the deletion query using the service.
 * @param id The identifier of the item to be deleted.
 * @param apiFilter An optional API filter, used to refine the query parameters. Defaults to an instance of the `FILT` type.
 * @param transform A transformation function applied to the `ItemState` result of the delete operation.
 * @return A `Promise` containing the transformed result of the delete operation.
 */
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

/**
 * Executes a create action for a given item utilizing the specified API service and applies a transformation to the resulting item state.
 *
 * @param CC The container type that manages the item, extending ICommonContainer.
 * @param T The type of the item to be created, which must extend BaseDoc.
 * @param ID The type of the ID field of the item, which must be a non-nullable type.
 * @param FILT The type of the API filter used for querying, must extend IApiFilter.
 * @param AIS The type of the API service, which must extend IApiCommonService.
 * @param R The return type resulting from the transformation function.
 * @param serviceManager The manager responsible for providing the appropriate API service.
 * @param apiItemFun A suspend function provided by the API service that operates on an API item and returns its state.
 * @param item The item to be created.
 * @param apiFilter Optional, the API filter to apply during the operation. Defaults to an instance created by apiFilterInstance().
 * @param transform A function to transform the resulting ItemState into the desired return type.
 * @return A Promise that resolves to the transformed result of the item state.
 */
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

/**
 * Updates an item within the API by applying the given action and transforms the resulting state.
 * The updated item's state is obtained through the provided service manager and API function.
 *
 * @param CC The type of the common container that manages the items.
 * @param T The type of the item being updated, which must extend BaseDoc.
 * @param ID The type of the item's identifier, which must be a non-nullable type.
 * @param FILT The type of the API filter used for the operation, which must extend IApiFilter.
 * @param AIS The type of the API service used for this operation.
 * @param R The type of the result after applying the transformation function.
 * @param serviceManager The manager handling API services used for executing the update.
 * @param apiItemFun A suspending function that defines the API item update logic.
 * @param item The item to be updated.
 * @param apiFilter The filter used for the API operation, defaults to an instance of FILT.
 * @param orig The original item version before the update, if available.
 * @param transform A function that processes the resulting ItemState to produce a result of type R.
 * @return A Promise of the result after applying the transformation function to the item’s updated state.
 */
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

/**
 * Executes an API delete call for a specific item and transforms the resulting item state.
 *
 * @param CC The container type implementing ICommonContainer.
 * @param T The type of the item being deleted, which must extend BaseDoc.
 * @param ID The type of the item's identifier, which must be a non-nullable type.
 * @param FILT The type of the API filter used for the operation, which must extend IApiFilter.
 * @param AIS The type of the API service being used, which must implement IApiCommonService.
 * @param R The return type resulting from the transformation applied to the item state.
 *
 * @param serviceManager The KVServiceManager instance managing the corresponding service.
 * @param apiItemFun A suspend function representing the API action to perform the delete operation.
 * @param item The item to be deleted, which must be of type T.
 * @param apiFilter The filter applied during the delete operation, which defaults to a new instance.
 * @param transform A transformation function applied to the resulting ItemState to produce the returned value.
 *
 * @return A Promise of the transformed result of type R.
 */
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
