package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.CrudTask
import com.fonrouge.fsLib.model.IDataItem
import com.fonrouge.fsLib.model.apiData.ApiFilter
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
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromDynamic
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

abstract class ConfigViewItem<T : BaseDoc<ID>, ID : Any, V : ViewItem<T, ID, FILT>, E : IDataItem, FILT : ApiFilter>(
    itemKClass: KClass<T>,
    idKClass: KClass<ID>,
    apiFilterKClass: KClass<FILT>,
    label: String,
    viewFunc: KClass<out V>,
    baseUrl: String = viewFunc.simpleName!!,
    requireCredentials: Boolean,
    private val serviceManager: KVServiceManager<E>,
    private val function: suspend E.(ApiItem<T, ID, FILT>) -> ItemState<T>,
    val labelIdFunc: ((T?) -> String?)? = { it?._id?.toString() ?: "<no-item>" },
) : ConfigViewContainer<T, V, ID, FILT>(
    itemKClass = itemKClass,
    idKClass = idKClass,
    apiFilterKClass = apiFilterKClass,
    name = itemKClass.simpleName!!,
    label = label,
    viewFunc = viewFunc,
    baseUrl = baseUrl,
    requireCredentials = requireCredentials
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

    @OptIn(InternalSerializationApi::class)
    fun urlRead(id: ID): String {
        val urlParams =
            UrlParams("id" to Json.encodeToString(idKClass.serializer(), id), "action" to CrudTask.Read.name)
        return url + urlParams.toString()
    }

    @OptIn(InternalSerializationApi::class)
    @Suppress("unused")
    fun urlDelete(id: ID): String {
        val urlParams =
            UrlParams("id" to Json.encodeToString(idKClass.serializer(), id), "action" to CrudTask.Delete.name)
        return url + urlParams.toString()
    }

    @OptIn(InternalSerializationApi::class)
    fun urlUpdate(id: ID): String {
        val urlParams =
            UrlParams("id" to Json.encodeToString(idKClass.serializer(), id), "action" to CrudTask.Update.name)
        return url + urlParams.toString()
    }

    @Suppress("unused")
    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    fun callItemService(
        crudTask: CrudTask,
        callType: ApiItem.CallType,
        id: ID? = null,
        item: T? = null,
        apiFilter: FILT,
        block: (ItemState<T>) -> ItemState<T>,
    ) {
        val (url, method) = serviceManager.requireCall(function)
        val callAgent = CallAgent()
        val paramList = listOf(
            Json.encodeToString(
                serializer = ApiItem.serializer(
                    itemKClass.serializer(),
                    idKClass.serializer(),
                    apiFilterKClass.serializer()
                ),
                value = ApiItem(
                    id = id,
                    item = item,
                    callType = callType,
                    crudTask = crudTask,
                    apiFilter = apiFilter,
                )
            ),
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
inline fun <reified T : BaseDoc<ID>, reified ID : Any, reified V : ViewItem<T, ID, FILT>, E : IDataItem, reified FILT : ApiFilter> configViewItem(
    itemKClass: KClass<T> = T::class,
    idKClass: KClass<ID> = ID::class,
    apiFilterKClass: KClass<FILT> = FILT::class,
    label: String,
    viewFunc: KClass<out V> = V::class,
    baseUrl: String = viewFunc.simpleName!!,
    requireCredentials: Boolean = true,
    serviceManager: KVServiceManager<E>,
    noinline function: suspend E.(ApiItem<T, ID, FILT>) -> ItemState<T>,
    noinline labelIdFunc: ((T?) -> String?)? = { it?._id?.toString() ?: "<no-item>" },
): ConfigViewItem<T, ID, V, E, FILT> = object : ConfigViewItem<T, ID, V, E, FILT>(
    itemKClass = itemKClass,
    idKClass = idKClass,
    apiFilterKClass = apiFilterKClass,
    label = label,
    viewFunc = viewFunc,
    baseUrl = baseUrl,
    requireCredentials = requireCredentials,
    serviceManager = serviceManager,
    function = function,
    labelIdFunc = labelIdFunc,
) {}
