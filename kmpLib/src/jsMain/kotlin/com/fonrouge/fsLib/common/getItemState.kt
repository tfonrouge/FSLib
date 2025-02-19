package com.fonrouge.fsLib.common

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
 * Retrieves the state of a specified API item from a remote service, transforms it,
 * and returns the transformed result as a promise.
 *
 * @param serviceManager The service manager used for managing remote calls.
 * @param apiItemFun A suspend function that retrieves the state of an API item.
 * @param apiItem The API item to retrieve the state for, containing necessary metadata.
 * @param transform A transformation function to apply to the retrieved item state before returning it.
 * @return A promise containing the transformed item state.
 */
@OptIn(ExperimentalSerializationApi::class)
fun <T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : Any, R : Any> ICommonContainer<T, ID, FILT>.getItemState(
    serviceManager: KVServiceManager<AIS>,
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    apiItem: ApiItem<T, ID, FILT>,
    transform: (ItemState<T>) -> R
): Promise<R> {
    val (url, method) = serviceManager.requireCall(apiItemFun)
    val callAgent = CallAgent()
    val iApiItem = toIApiItem<T, ID, FILT>(apiItem)
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
