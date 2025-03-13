package com.fonrouge.fsLib.common

import com.fonrouge.fsLib.config.ConfigViewList
import com.fonrouge.fsLib.model.apiData.ApiList
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ListState
import com.fonrouge.fsLib.model.state.State
import io.kvision.remote.CallAgent
import io.kvision.remote.JsonRpcRequest
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import io.kvision.utils.Serialization
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromDynamic
import kotlin.js.Promise

/**
 * Retrieves a transformed representation of the state of a list retrieved from an API function.
 *
 * @param apiListFun A suspend function specifying the API call that retrieves the list state.
 * @param apiList An instance of ApiList containing the filter, sorter, and pagination details for the API call. Defaults to a new ApiList with the default filter.
 * @param transform A transformation function to convert the retrieved ListState into the desired return format.
 * @return A Promise that resolves to the result of the transformation function applied to the ListState.
 */
@Suppress("unused")
@OptIn(ExperimentalSerializationApi::class)
fun <T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : Any, R : Any> ICommonContainer<T, ID, FILT>.getListState(
    apiListFun: suspend AIS.(ApiList<FILT>) -> ListState<T>,
    apiList: ApiList<FILT> = ApiList(apiFilter = apiFilterInstance()),
    transform: (ListState<T>) -> R,
): Promise<R> {
    val (url, method) = ConfigViewList.serviceManager.requireCall(apiListFun)
    val callAgent = CallAgent()
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
        transform(
            listState ?: ListState(
                data = listOf(),
                last_page = null,
                last_row = null,
                state = State.Error
            )
        )
    }
}
