package com.fonrouge.fsLib.common

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

@OptIn(ExperimentalSerializationApi::class)
fun <T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : Any, R : Any> ICommonContainer<T, ID, FILT>.getItemState(
    serviceManager: KVServiceManager<AIS>,
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    id: ID,
    apiFilter: FILT = apiFilterInstance(),
    transform: (ItemState<T>) -> R
): Promise<R> {
    val (url, method) = serviceManager.requireCall(apiItemFun)
    val callAgent = CallAgent()
    val apiItem = iApiItemQueryRead(id, apiFilter)
    val apiItemSerialized =
        Json.encodeToString(IApiItem.serializer(itemSerializer, idSerializer, apiFilterSerializer), apiItem)
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
//        Promise.resolve(
//            transform(
//                Json.decodeFromDynamic(
//                    ItemState.serializer(itemSerializer),
//                    result
//                )
//            )
//        )
        transform(
            Json.decodeFromDynamic(
                ItemState.serializer(itemSerializer),
                result
            )
        )
    }
}
