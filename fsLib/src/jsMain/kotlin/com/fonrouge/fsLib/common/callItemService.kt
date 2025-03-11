package com.fonrouge.fsLib.common

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
 * Executes an API call for an item-based service using the provided function, CRUD task, and call type.
 *
 * @param T The type of the item, which must extend BaseDoc.
 * @param ID The type of the ID of the item, which must be non-nullable.
 * @param FILT The type of the API filter, which must extend IApiFilter.
 * @param serviceManager The service manager used for retrieving API call configuration, such as the URL and method.
 * @param apiItemFun The function representing the API call.
 * @param crudTask The CRUD operation to be performed (Create, Read, Update, or Delete).
 * @param callType The type of API call, either Query or Action.
 * @param id An optional ID of the item for Read, Update, or Delete operations.
 * @param item An optional item to be passed for Create, Update, or Delete operations in the Action call type.
 * @param orig An optional original item for auditing or comparison during Update operations in the Action call type.
 * @param apiFilter An optional API filter to refine or customize the API call; defaults to a new instance using apiFilterInstance.
 * @param block A callback function to handle the final state of the item after the API call.
 */
@OptIn(ExperimentalSerializationApi::class)
fun <T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> ICommonContainer<T, ID, FILT>.callItemService(
    serviceManager: KVServiceManager<*>,
    apiItemFun: Function<*>,
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
            CrudTask.Create -> apiItemQueryCreate(apiFilter)
            CrudTask.Read -> id?.let { apiItemQueryRead(id, apiFilter) }
            CrudTask.Update -> id?.let { apiItemQueryUpdate(id, apiFilter) }
            CrudTask.Delete -> id?.let { apiItemQueryDelete(id, apiFilter) }
        }

        CallType.Action -> when (crudTask) {
            CrudTask.Create -> item?.let {
                apiItemActionCreate(
                    item,
                    apiFilter
                )
            }

            CrudTask.Read -> null
            CrudTask.Update -> item?.let {
                apiItemActionUpdate(
                    item,
                    apiFilter,
                    orig
                )
            }

            CrudTask.Delete -> item?.let {
                apiItemActionDelete(
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
            value = toIApiItem(iApiItem)
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
