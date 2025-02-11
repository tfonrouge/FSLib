package com.fonrouge.fsLib.common

import com.fonrouge.fsLib.model.apiData.ApiList
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ListState
import com.fonrouge.fsLib.model.state.State
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
import kotlin.js.Promise

/**
 * Retrieves a data list from the server based on the provided filter, applies a transformation
 * to the data, and returns it as a promise.
 *
 * @param serviceManager The service manager responsible for making the API call.
 * @param apiListFun The suspend function that defines the API call to be made.
 * @param apiFilter The filter to be applied to the API call.
 * @param transform A function to transform the retrieved data into the desired result type.
 * @return A promise that resolves to the transformed data.
 */
@Suppress("unused")
@OptIn(ExperimentalSerializationApi::class)
fun <T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : Any, R : Any> ICommonContainer<T, ID, FILT>.getListState(
    serviceManager: KVServiceManager<AIS>,
    apiListFun: suspend AIS.(ApiList<FILT>) -> ListState<T>,
    apiFilter: FILT = apiFilterInstance(),
    transform: (ListState<T>) -> R,
): Promise<Promise<R>> {
    val (url, method) = serviceManager.requireCall(apiListFun)
    val callAgent = CallAgent()
    val apiList = ApiList(apiFilter = apiFilter)
    val apiListSerialized = Json.encodeToString(ApiList.serializer(apiFilterSerializer), apiList)
    val paramList = listOf(apiListSerialized)
    val data = Serialization.plain.encodeToString(
        JsonRpcRequest(
            id = 0,
            method = url,
            params = paramList
        )
    )
    return callAgent.remoteCall(url = url, data = data, method = method).then { d: dynamic ->
        val result = JSON.parse<dynamic>(d.result.unsafeCast<String>())
        val listState: ListState<T>? = if (d.error != null) {
            Toast.danger(
                message = "Server error ${d.error}",
                options = ToastOptions(
                    position = ToastPosition.BOTTOMRIGHT,
                    escapeHtml = true,
                    duration = 10000,
                    stopOnFocus = true,
                    newWindow = true
                )
            )
            null
        } else {
            try {
                Json.decodeFromDynamic(
                    ListState.serializer(itemSerializer),
                    result
                )
            } catch (e: Exception) {
                console.error(
                    "Error decoding KClass",
                    itemSerializer,
                    "with serialized value",
                )
                null
            }
        }
        Promise.resolve(
            transform(
                listState ?: ListState(
                    data = listOf(),
                    last_page = null,
                    last_row = null,
                    state = State.Error
                )
            )
        )
    }
}
