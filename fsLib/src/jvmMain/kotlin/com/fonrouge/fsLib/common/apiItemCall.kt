package com.fonrouge.fsLib.common

import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.apiData.IApiItem
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import kotlinx.serialization.json.Json

/**
 * Executes a create operation for an API item using the specified query function and API filter.
 *
 * @param service The service instance implementing the `IApiCommonService` interface, used to handle the API call.
 * @param apiItemFun A suspend function representing the create operation logic. This function takes an instance of `IApiItem` and returns an `ItemState`.
 * @param apiFilter An optional API filter of type `FILT` used for the create query. Defaults to the result of `apiFilterInstance()`.
 * @return An `ItemState` object representing the result of the create operation.
 */
@Suppress("unused")
suspend fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : Any> CC.apiItemQueryCreateCall(
    service: AIS,
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    id: ID? = null,
    apiFilter: FILT = apiFilterInstance(),
): ItemState<T> = apiItemFun(
    service,
    IApiItem.Upsert.Create.Query(
        serializedId = id?.let { Json.encodeToString(idSerializer, id) },
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter)
    )
)

/**
 * Executes a read operation for a specific item by its ID using a provided service and API filter.
 *
 * @param CC The container type implementing ICommonContainer that holds management logic for items.
 * @param T The type of the item being queried, which must extend BaseDoc.
 * @param ID The type of the ID of the item, which must be non-nullable.
 * @param FILT The type of the filter, extending IApiFilter, which defines constraints for the query.
 * @param AIS The service type implementing IApiCommonService that processes the API call.
 * @param service The instance of the service to be used for processing the query.
 * @param apiItemFun A suspend function provided by the service that takes an API item and returns its state.
 *                   This is the function responsible for handling the read operation.
 * @param id The unique identifier of the item to be read.
 * @param apiFilter The filter used to define additional constraints for the query, defaulting to an instance of the filter type.
 *
 * @return The state of the queried item, wrapped in an ItemState instance.
 */
@Suppress("unused")
suspend fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : Any> CC.apiItemQueryReadCall(
    service: AIS,
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    id: ID,
    apiFilter: FILT = apiFilterInstance(),
): ItemState<T> = apiItemFun(
    service,
    IApiItem.Read(
        serializedId = Json.encodeToString(idSerializer, id),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter)
    )
)

/**
 * Makes an API call that performs an update operation on an item using a query-based approach.
 *
 * @param CC The container type that must implement `ICommonContainer`.
 * @param T The type of the items managed by the container, must extend `BaseDoc`.
 * @param ID The type of the item ID, must be non-nullable.
 * @param FILT The type of the API filter, must extend `IApiFilter`.
 * @param AIS The type of the API service, must implement `IApiCommonService`.
 * @param service The service instance used to handle the API request.
 * @param apiItemFun A suspend function representing the actual API item update logic.
 * @param id The identifier of the item to be updated.
 * @param apiFilter The API filter to be applied for the query, defaults to an instance created by `apiFilterInstance`.
 * @return An `ItemState` representing the result state of the updated item.
 */
@Suppress("unused")
suspend fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : Any> CC.apiItemQueryUpdateCall(
    service: AIS,
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    id: ID,
    apiFilter: FILT = apiFilterInstance(),
): ItemState<T> = apiItemFun(
    service,
    IApiItem.Upsert.Update.Query(
        serializedId = Json.encodeToString(idSerializer, id),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter)
    )
)

/**
 * Executes a delete API query call for a specific item within the container.
 *
 * @param service The service implementing the common API operations.
 * @param apiItemFun A suspending function defined on the service to handle the API item deletion logic.
 * @param id The unique identifier of the item to be deleted.
 * @param apiFilter An optional API filter object used to apply additional conditions or constraints to the delete operation. Defaults to an instance of the filter.
 * @return The resulting state of the item after the delete operation, encapsulated in an ItemState object.
 */
@Suppress("unused")
suspend fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : Any> CC.apiItemQueryDeleteCall(
    service: AIS,
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    id: ID,
    apiFilter: FILT = apiFilterInstance(),
): ItemState<T> = apiItemFun(
    service,
    IApiItem.Delete.Query(
        serializedId = Json.encodeToString(idSerializer, id),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter)
    )
)

/**
 * Performs a create operation for an API item in the context of a given service.
 *
 * @param CC The type of the common container.
 * @param T The type of the item to be created. Must extend BaseDoc.
 * @param ID The type of the unique identifier of the item. Must be a non-nullable type.
 * @param FILT The type of the filter used in the API operation. Must extend IApiFilter.
 * @param AIS The type of the service being used for the operation. Must implement IApiCommonService.
 * @param service The service instance used to process the API operation.
 * @param apiItemFun A suspend function that performs the API item action for creation.
 * @param item The item to be created in the API.
 * @param apiFilter The filter instance to apply to the create operation. Defaults to an instance created by `apiFilterInstance()`.
 * @return An ItemState representing the result of the create operation.
 */
@Suppress("unused")
suspend fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : Any> CC.apiItemActionCreateCall(
    service: AIS,
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    item: T,
    apiFilter: FILT = apiFilterInstance(),
): ItemState<T> = apiItemFun(
    service,
    IApiItem.Upsert.Create.Action(
        serializedItem = Json.encodeToString(itemSerializer, item),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter)
    )
)

/**
 * Invokes an update action on an API item using the specified service and input parameters.
 * This method is a coroutine and performs the API operation in a suspendable context.
 *
 * @param CC The container type that manages API items.
 * @param T The type of the items being updated, which extends BaseDoc.
 * @param ID The type of the identifier for the items, which must be non-nullable.
 * @param FILT The type of the API filter used in the operation, which must extend IApiFilter.
 * @param AIS The type of the API service handling the operation, which must extend IApiCommonService.
 * @param service The API service instance used for invoking the update action.
 * @param apiItemFun A suspendable function defining the update logic for a given API item.
 * @param item The item to update.
 * @param apiFilter The API filter to be applied during the update operation. Defaults to an instance of the filter type.
 * @param orig The original version of the item before the update, used for comparison or additional context. Can be null.
 * @return The resulting state of the item after the update action.
 */
@Suppress("unused")
suspend fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : Any> CC.apiItemActionUpdateCall(
    service: AIS,
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
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

/**
 * Handles the deletion of a specific item via an API call.
 *
 * @param service The service instance of type [AIS] responsible for executing the API call.
 * @param apiItemFun A suspend function provided by the service, defining the delete operation for the item.
 * @param item The item of type [T], extending [BaseDoc], which is to be deleted.
 * @param apiFilter The filter of type [FILT], extending [IApiFilter], used to customize the API request. Defaults to a newly created filter instance.
 * @return The resulting state of the item after the API call, encapsulated in an [ItemState] instance.
 */
@Suppress("unused")
suspend fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : Any> CC.apiItemActionDeleteCall(
    service: AIS,
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    item: T,
    apiFilter: FILT = apiFilterInstance(),
): ItemState<T> = apiItemFun(
    service,
    IApiItem.Delete.Action(
        serializedItem = Json.encodeToString(itemSerializer, item),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter)
    )
)
