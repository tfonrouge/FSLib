package com.fonrouge.fsLib.common

import com.fonrouge.fsLib.commonServices.IApiCommonService
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.apiData.IApiItem
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import io.kvision.remote.CallAgent
import io.kvision.remote.JsonRpcRequest
import io.kvision.remote.KVServiceManager
import io.kvision.utils.Serialization
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromDynamic
import kotlin.js.Promise

/**
 * Retrieves the state of a specific item using the provided API method and service manager.
 *
 * @param serviceManager The service manager responsible for managing API calls.
 * @param apiItemFun A suspend function to be executed on the API service for retrieving the item's state.
 * @param apiItem The API item containing information about the item for which the state is being retrieved.
 * @param transform A function to transform the resulting item state into the desired return type.
 * @return A promise representing the transformed item state resulting from the API call.
 */
@OptIn(ExperimentalSerializationApi::class)
fun <T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : IApiCommonService, R : Any?> ICommonContainer<T, ID, FILT>.getItemState(
    serviceManager: KVServiceManager<AIS>,
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    apiItem: ApiItem<T, ID, FILT>,
    transform: (ItemState<T>) -> R
): Promise<R> {
    val (url, method) = serviceManager.requireCall(apiItemFun)
    val callAgent = CallAgent()
    val iApiItem = toIApiItem(apiItem)
    val apiItemSerialized =
        Json.encodeToString(IApiItem.serializer(itemSerializer, idSerializer, apiFilterSerializer), iApiItem)
    val paramList = listOf(apiItemSerialized)
    val data = Serialization.plain.encodeToString(
        JsonRpcRequest(
            id = 0,
            method = url,
            params = paramList
        )
    )
    return callAgent.remoteCall(url = url, data = data, method = method).then { r: dynamic ->
        val result = JSON.parse<dynamic>(r.result.unsafeCast<String>())
        transform(
            Json.decodeFromDynamic(
                ItemState.serializer(itemSerializer),
                result
            )
        )
    }
}
