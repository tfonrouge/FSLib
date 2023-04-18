package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.CrudTask
import com.fonrouge.fsLib.model.IDataItem
import com.fonrouge.fsLib.model.ItemResponse
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.StateItem
import com.fonrouge.fsLib.view.ViewItem
import io.kvision.remote.CallAgent
import io.kvision.remote.HttpMethod
import io.kvision.remote.JsonRpcRequest
import io.kvision.remote.KVServiceManager
import io.kvision.utils.Serialization
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromDynamic
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

abstract class ConfigViewItem<T : BaseDoc<U>, V : ViewItem<T, U>, E : IDataItem, U : Any>(
    val itemKClass: KClass<T>,
    idKClass: KClass<U>? = null,
    label: String,
    viewFunc: KClass<out V>,
    baseUrl: String = viewFunc.simpleName!!,
    private val serviceManager: KVServiceManager<E>,
    private val function: suspend E.(U?, StateItem<T>) -> ItemResponse<T>,
    private val stateFunction: (() -> String)? = null,
    val labelIdFunc: ((T?) -> String?)? = { it?._id?.toString() ?: "<no-item>" }
) : ConfigViewContainer<T, V, U>(
    idKClass = idKClass,
    name = itemKClass.simpleName!!,
    label = label,
    viewFunc = viewFunc,
    baseUrl = baseUrl
) {
    companion object {
        val configViewItemMap = mutableMapOf<String, ConfigViewItem<*, *, *, *>>()
    }

    val labelDelete = "Delete $label"
    val labelDetail = "Detail of $label"
    val labelCreate = "Create $label"
    val labelUpdate = "Update $label"

    @Suppress("unused")
    fun labelUrlRead(id: U) = label to urlRead(id)

    @Suppress("unused")
    fun labelUrlUpdate(id: U) = label to urlUpdate(id)

    @Suppress("unused")
    val urlCreate: String
        get() {
            val urlParams = UrlParams("action" to CrudTask.Create.name)
            return url + urlParams.toString()
        }

    fun urlRead(id: U): String {
        val urlParams = UrlParams("id" to encodedId(id), "action" to CrudTask.Read.name)
        return url + urlParams.toString()
    }

    @Suppress("unused")
    fun urlDelete(id: U): String {
        val urlParams = UrlParams("id" to encodedId(id), "action" to CrudTask.Delete.name)
        return url + urlParams.toString()
    }

    fun urlUpdate(id: U): String {
        val urlParams = UrlParams("id" to encodedId(id), "action" to CrudTask.Update.name)
        return url + urlParams.toString()
    }

    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    fun callItemService(
        crudTask: CrudTask,
        callType: StateItem.CallType,
        itemId: String? = JSON.stringify(null),
        item: T? = null,
        urlParams: UrlParams? = null,
        block: (ItemResponse<T>) -> ItemResponse<T>,
    ) {
        val (url, method) = serviceManager.requireCall(function)
        val callAgent = CallAgent()
        val paramList = listOf(
            itemId,
            Json.encodeToString(
                serializer = StateItem.serializer(itemKClass.serializer()),
                value = StateItem(
                    item = item,
                    callType = callType,
                    crudTask = crudTask,
                    contextClass = urlParams?.contextClass,
                    contextId = urlParams?.contextId,
                    state = stateFunction?.invoke(),
                )
            )
        )
        val data = Serialization.plain.encodeToString(
            JsonRpcRequest(
                id = 0,
                method = url,
                params = paramList
            )
        )
        callAgent.remoteCall(url, data, method = HttpMethod.valueOf(method.name)).then { r: dynamic ->
            val result = JSON.parse<dynamic>(r.result.unsafeCast<String>())
            try {
                val itemResponse: ItemResponse<T> =
                    Json.decodeFromDynamic(ItemResponse.serializer(itemKClass.serializer()), result)
                block(itemResponse)
            } catch (e: Exception) {
                console.error("Error decoding KClass", itemKClass, "with serialized value", result, "exception:", e)
                e.printStackTrace()
            }
        }
    }

    init {
        configViewItemMap[baseUrl] = this
    }
}
