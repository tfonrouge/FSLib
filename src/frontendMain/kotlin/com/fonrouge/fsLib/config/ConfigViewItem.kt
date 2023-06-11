package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.CrudTask
import com.fonrouge.fsLib.model.IDataItem
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import com.fonrouge.fsLib.view.ViewItem
import io.kvision.remote.CallAgent
import io.kvision.remote.HttpMethod
import io.kvision.remote.JsonRpcRequest
import io.kvision.remote.KVServiceManager
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import io.kvision.utils.Serialization
import js.uri.decodeURIComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromDynamic
import kotlinx.serialization.serializer
import web.buffer.atob
import kotlin.reflect.KClass

abstract class ConfigViewItem<T : BaseDoc<ID>, V : ViewItem<T, ID, FILT>, E : IDataItem, ID : Any, FILT : Any>(
    val itemKClass: KClass<T>,
    idKClass: KClass<ID>? = null,
    apiFilterKClass: KClass<FILT>,
    label: String,
    viewFunc: KClass<out V>,
    baseUrl: String = viewFunc.simpleName!!,
    private val serviceManager: KVServiceManager<E>,
    private val function: suspend E.(ID?, ApiItem<T>, FILT) -> ItemState<T>,
    private val stateFunction: (() -> String)? = null,
    val labelIdFunc: ((T?) -> String?)? = { it?._id?.toString() ?: "<no-item>" },
) : ConfigViewContainer<T, V, ID, FILT>(
    idKClass = idKClass,
    apiFilterKClass = apiFilterKClass,
    name = itemKClass.simpleName!!,
    label = label,
    viewFunc = viewFunc,
    baseUrl = baseUrl
) {
    companion object {
        val configViewItemMap = mutableMapOf<String, ConfigViewItem<*, *, *, *, *>>()
        val a: KClass<Unit> = Unit::class
    }

    val labelDelete = "Delete $label"
    val labelDetail = "Detail of $label"
    val labelCreate = "Create $label"
    val labelUpdate = "Update $label"

    @Suppress("unused")
    fun labelUrlRead(id: ID) = label to urlRead(id)

    @Suppress("unused")
    fun labelUrlUpdate(id: ID) = label to urlUpdate(id)

    @Suppress("unused")
    val urlCreate: String
        get() {
            val urlParams = UrlParams("action" to CrudTask.Create.name)
            return url + urlParams.toString()
        }

    fun urlRead(id: ID): String {
        val urlParams = UrlParams("id" to encodedId(id), "action" to CrudTask.Read.name)
        return url + urlParams.toString()
    }

    @Suppress("unused")
    fun urlDelete(id: ID): String {
        val urlParams = UrlParams("id" to encodedId(id), "action" to CrudTask.Delete.name)
        return url + urlParams.toString()
    }

    fun urlUpdate(id: ID): String {
        val urlParams = UrlParams("id" to encodedId(id), "action" to CrudTask.Update.name)
        return url + urlParams.toString()
    }

    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    fun callItemService(
        crudTask: CrudTask,
        callType: ApiItem.CallType,
        itemId: String? = JSON.stringify(null),
        item: T? = null,
        urlParams: UrlParams? = null,
        apiFilterSerialized: String?,
        block: (ItemState<T>) -> ItemState<T>,
    ) {
        val (url, method) = serviceManager.requireCall(function)
        val callAgent = CallAgent()
        val paramList = listOf(
            itemId,
            Json.encodeToString(
                serializer = ApiItem.serializer(itemKClass.serializer()),
                value = ApiItem(
                    item = item,
                    callType = callType,
                    crudTask = crudTask,
                    contextClass = urlParams?.contextClass,
                    contextId = urlParams?.contextId,
                    state = stateFunction?.invoke(),
                )
            ),
            apiFilterSerialized?.let { atob(decodeURIComponent(it)) }
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
                        ItemState.serializer(itemKClass.serializer()),
                        result
                    )
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

@Suppress("unused")
fun <T : BaseDoc<ID>, V : ViewItem<T, ID, FILT>, E : IDataItem, ID : Any, FILT : Any> configViewItem(
    itemKClass: KClass<T>,
    idKClass: KClass<ID>? = null,
    apiFilterKClass: KClass<FILT>,
    label: String,
    viewFunc: KClass<out V>,
    baseUrl: String = viewFunc.simpleName!!,
    serviceManager: KVServiceManager<E>,
    function: suspend E.(ID?, ApiItem<T>, FILT) -> ItemState<T>,
    stateFunction: (() -> String)? = null,
    labelIdFunc: ((T?) -> String?)? = { it?._id?.toString() ?: "<no-item>" },
): ConfigViewItem<T, V, E, ID, FILT> = object : ConfigViewItem<T, V, E, ID, FILT>(
    itemKClass = itemKClass,
    idKClass = idKClass,
    apiFilterKClass = apiFilterKClass,
    label = label,
    viewFunc = viewFunc,
    baseUrl = baseUrl,
    serviceManager = serviceManager,
    function = function,
    stateFunction = stateFunction,
    labelIdFunc = labelIdFunc,
) {}
