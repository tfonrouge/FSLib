package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.CrudTask
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.IApiFilter
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromDynamic
import kotlin.reflect.KClass

abstract class ConfigViewItem<CV : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, V : ViewItem<CV, T, ID, FILT>, E : Any, FILT : IApiFilter>(
    private val serviceManager: KVServiceManager<E>,
    private val function: suspend E.(ApiItem<T, ID, FILT>) -> ItemState<T>,
    override val commonView: CV,
    viewFunc: KClass<out V>,
    baseUrl: String? = null
) : ConfigViewContainer<CV, T, ID, V, FILT>(
    viewFunc = viewFunc,
    commonView = commonView,
    baseUrl = baseUrl,
) {
    override val baseUrl: String
        get() {
            val result =
                _baseUrl
                    ?: if (commonView == undefined) "error: commonView undefined" else ("ViewItem" + commonView.name)
            return result
        }

    companion object {
        val configViewItemMap = mutableMapOf<String, ConfigViewItem<*, *, *, *, *, *>>()
    }

    override val label: String get() = commonView.labelItem
    val labelDelete by lazy { "Delete ${commonView.labelItem}" }
    val labelDetail by lazy { "Detail of ${commonView.labelIdFunc}" }
    val labelCreate by lazy { "Create ${commonView.labelItem}" }
    val labelUpdate by lazy { "Update ${commonView.labelItem}" }

    override val labelUrl: Pair<String, String> by lazy { commonView.labelItem to url }

    @Suppress("unused")
    fun labelUrlRead(id: ID) = commonView.labelItem to urlRead(id)

    @Suppress("unused")
    fun labelUrlUpdate(id: ID) = commonView.labelItem to urlUpdate(id)

    @Suppress("unused")
    val urlCreate: String
        get() {
            val urlParams = UrlParams("action" to CrudTask.Create.name)
            return url + urlParams.toString()
        }

    fun urlRead(id: ID): String {
        val urlParams =
            UrlParams("id" to Json.encodeToString(commonView.idSerializer, id), "action" to CrudTask.Read.name)
        return url + urlParams.toString()
    }

    @Suppress("unused")
    fun urlDelete(id: ID): String {
        val urlParams =
            UrlParams("id" to Json.encodeToString(commonView.idSerializer, id), "action" to CrudTask.Delete.name)
        return url + urlParams.toString()
    }

    fun urlUpdate(id: ID): String {
        val urlParams =
            UrlParams("id" to Json.encodeToString(commonView.idSerializer, id), "action" to CrudTask.Update.name)
        return url + urlParams.toString()
    }

    @Suppress("unused")
    @OptIn(ExperimentalSerializationApi::class)
    fun callItemService(
        crudTask: CrudTask,
        callType: ApiItem.CallType,
        id: ID? = null,
        item: T? = null,
        apiFilter: FILT? = null,
        block: (ItemState<T>) -> ItemState<T>,
    ) {
        val (url, method) = serviceManager.requireCall(function)
        val callAgent = CallAgent()
        val paramList = listOf(
            Json.encodeToString(
                serializer = ApiItem.serializer(
                    commonView.itemSerializer,
                    commonView.idSerializer,
                    commonView.apiFilterSerializer
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
                        ItemState.serializer(commonView.itemSerializer),
                        result
                    )
                block(itemResponse)
            } catch (e: Exception) {
                console.error(
                    "Error decoding KClass",
                    commonView.itemSerializer,
                    "with serialized value",
                    result,
                    "exception:",
                    e
                )
                e.printStackTrace()
            }
        }
    }

    init {
        console.warn("ConfigViewItem REGISTERING WITH", this.baseUrl)
        configViewItemMap[this.baseUrl] = this
    }
}

@Suppress("unused")
fun <CV : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, V : ViewItem<CV, T, ID, FILT>, E : Any, FILT : IApiFilter> configViewItem(
    viewFunc: KClass<out V>,
    serviceManager: KVServiceManager<E>,
    function: suspend E.(ApiItem<T, ID, FILT>) -> ItemState<T>,
    commonView: CV,
    baseUrl: String? = null
): ConfigViewItem<CV, T, ID, V, E, FILT> = object : ConfigViewItem<CV, T, ID, V, E, FILT>(
    viewFunc = viewFunc,
    serviceManager = serviceManager,
    function = function,
    commonView = commonView,
    baseUrl = baseUrl
) {}
