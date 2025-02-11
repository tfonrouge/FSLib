package com.fonrouge.fsLib.common

import com.fonrouge.fsLib.commonServices.IApiCommonService
import com.fonrouge.fsLib.model.apiData.CallType
import com.fonrouge.fsLib.model.apiData.CrudTask
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.apiData.IApiItem
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import io.kvision.remote.CallAgent
import io.kvision.remote.JsonRpcRequest
import io.kvision.remote.KVServiceManager
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import io.kvision.utils.Serialization
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromDynamic

/**
 * Executes a transactional operation on the specified API service.
 * Used to perform CRUD tasks or actions by dynamically constructing the necessary API call.
 *
 * @param T The type of the base document.
 * @param ID The type of the unique identifier for the base document.
 * @param FILT The type of the API filter used for querying or actions.
 * @param AIS The type of the API service being called.
 * @param serviceManager The service manager responsible for handling API service call configurations.
 * @param apiItemFun The service function to be called, defined as an extension over the API service type.
 * @param crudTask The CRUD task to be performed, such as create, read, update, or delete.
 * @param callType The type of the call, either Query or Action.
 * @param id The unique identifier of the base document (used for Read/Update/Delete).
 * @param item The instance of the base document to be created or updated.
 * @param orig The original instance of the base document (used for update comparisons).
 * @param apiFilter The filter used for querying the API, with a default instance if not provided.
 * @param block A callback function that processes and optionally transforms the resulting item state.
 */
@OptIn(ExperimentalSerializationApi::class)
fun <T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : IApiCommonService> ICommonContainer<T, ID, FILT>.callItemService(
    serviceManager: KVServiceManager<AIS>,
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    crudTask: CrudTask,
    callType: CallType,
    id: ID? = null,
    item: T? = null,
    orig: T? = null,
    apiFilter: FILT = apiFilterInstance(),
    block: (ItemState<T>) -> ItemState<T>,
) {
    val (url, method) = serviceManager.requireCall(apiItemFun)
    val callAgent = CallAgent()
    val iApiItem = when (callType) {
        CallType.Query -> when (crudTask) {
            CrudTask.Create -> iApiItemQueryCreate(apiFilter)
            CrudTask.Read -> id?.let { iApiItemQueryRead(id, apiFilter) }
            CrudTask.Update -> id?.let { iApiItemQueryUpdate(id, apiFilter) }
            CrudTask.Delete -> id?.let { iApiItemQueryDelete(id, apiFilter) }
        }

        CallType.Action -> when (crudTask) {
            CrudTask.Create -> item?.let {
                iApiItemActionCreate(
                    item,
                    apiFilter
                )
            }

            CrudTask.Read -> null
            CrudTask.Update -> item?.let {
                iApiItemActionUpdate(
                    item,
                    apiFilter,
                    orig
                )
            }

            CrudTask.Delete -> item?.let {
                iApiItemActionDelete(
                    item,
                    apiFilter
                )
            }
        }
    } ?: return
    val paramList = listOf(
        Json.encodeToString(
            serializer = IApiItem.serializer(
                itemSerializer,
                idSerializer,
                apiFilterSerializer
            ),
            value = iApiItem
        ),
    )
    val data = Serialization.plain.encodeToString(
        JsonRpcRequest(
            id = 0,
            method = url,
            params = paramList
        )
    )
    callAgent.remoteCall(url = url, data = data, method = method).then { r: dynamic ->
        val result = JSON.parse<dynamic>(r.result.unsafeCast<String>())
        if (r.error != null) {
            console.error("Server error:", r.error)
            Toast.danger(
                message = "Server error ${r.error}",
                options = ToastOptions(
                    position = ToastPosition.BOTTOMRIGHT,
                    escapeHtml = true,
                    duration = 10000,
                    stopOnFocus = true,
                    newWindow = true
                )
            )
        }
        try {
            val itemResponse: ItemState<T> =
                Json.decodeFromDynamic(
                    ItemState.serializer(itemSerializer),
                    result
                )
            block(itemResponse)
        } catch (e: Exception) {
            console.error(
                "Error decoding KClass",
                itemSerializer,
                "with serialized value",
                result,
                "exception:",
                e
            )
            e.printStackTrace()
        }
    }
}
