package com.fonrouge.fsLib.common

import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.model.apiData.CallType
import com.fonrouge.fsLib.model.apiData.CrudTask
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.apiData.IApiItem
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import io.kvision.remote.CallAgent
import io.kvision.remote.JsonRpcRequest
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import io.kvision.utils.Serialization
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromDynamic

/**
 * Executes a service call for an API item within the container context.
 *
 * @param apiItemFun A reference to the function representing the API operation.
 * @param crudTask The type of CRUD operation to perform (Create, Read, Update, Delete).
 * @param callType The type of API call (Query or Action).
 * @param id The unique identifier of the item, or null if not applicable.
 * @param item The item to be used for the operation, or null if not applicable.
 * @param orig The original item before any updates, or null if not applicable.
 * @param apiFilter The API filter used to customize queries, defaults to an instance created from the container's apiFilterInstance method.
 * @param block A lambda to process the result of the operation, receiving the item state as input and returning the modified item state.
 */
@OptIn(ExperimentalSerializationApi::class)
fun <T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> ICommonContainer<T, ID, FILT>.callItemService(
    apiItemFun: Function<*>,
    crudTask: CrudTask,
    callType: CallType,
    id: ID? = null,
    item: T? = null,
    orig: T? = null,
    apiFilter: FILT = apiFilterInstance(),
    block: (ItemState<T>) -> ItemState<T>,
) {
    val (url, method) = ConfigViewItem.serviceManager.requireCall(apiItemFun)
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
