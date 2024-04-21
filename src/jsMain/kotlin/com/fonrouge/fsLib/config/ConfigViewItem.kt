package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.apiServices.IApiService
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.apiData.CallType
import com.fonrouge.fsLib.model.apiData.CrudTask
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.apiData.IApiItem
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

abstract class ConfigViewItem<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, V : ViewItem<CC, T, ID, FILT>, AIS : IApiService, FILT : IApiFilter>(
    private val serviceManager: KVServiceManager<AIS>,
    override val commonContainer: CC,
    private val apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    viewFunc: KClass<out V>,
    baseUrl: String? = null
) : ConfigViewContainer<CC, T, ID, V, FILT>(
    viewFunc = viewFunc,
    commonContainer = commonContainer,
    baseUrl = baseUrl,
) {
    override val baseUrl: String
        get() {
            return _baseUrl ?: viewFunc.simpleName!!
        }

    companion object {
        val configViewItemMap = mutableMapOf<String, ConfigViewItem<*, *, *, *, *, *>>()
    }

    override val label: String get() = commonContainer.labelItem
    val labelDelete by lazy { "Delete ${commonContainer.labelItem}" }
    val labelDetail by lazy { "Detail of ${commonContainer.labelIdFunc}" }
    val labelCreate by lazy { "Create ${commonContainer.labelItem}" }
    val labelUpdate by lazy { "Update ${commonContainer.labelItem}" }

    override val labelUrl: Pair<String, String> by lazy { commonContainer.labelItem to url }

    @Suppress("unused")
    fun labelUrlRead(id: ID) = commonContainer.labelItem to urlRead(id)

    @Suppress("unused")
    fun labelUrlUpdate(id: ID) = commonContainer.labelItem to urlUpdate(id)

    @Suppress("unused")
    val urlCreate: String
        get() {
            val urlParams = UrlParams("action" to CrudTask.Create.name)
            return url + urlParams.toString()
        }

    fun urlRead(id: ID): String {
        val urlParams =
            UrlParams("id" to Json.encodeToString(commonContainer.idSerializer, id), "action" to CrudTask.Read.name)
        return url + urlParams.toString()
    }

    @Suppress("unused")
    fun urlDelete(id: ID): String {
        val urlParams =
            UrlParams("id" to Json.encodeToString(commonContainer.idSerializer, id), "action" to CrudTask.Delete.name)
        return url + urlParams.toString()
    }

    fun urlUpdate(id: ID): String {
        val urlParams =
            UrlParams("id" to Json.encodeToString(commonContainer.idSerializer, id), "action" to CrudTask.Update.name)
        return url + urlParams.toString()
    }

    @Suppress("unused")
    @OptIn(ExperimentalSerializationApi::class)
    fun callItemService(
        crudTask: CrudTask,
        callType: CallType,
        id: ID? = null,
        item: T? = null,
        apiFilter: FILT = commonContainer.apiFilterInstance(),
        block: (ItemState<T>) -> ItemState<T>,
    ) {
        val (url, method) = serviceManager.requireCall(apiItemFun)
        val callAgent = CallAgent()
        val iApiItem = when (callType) {
            CallType.Query -> when (crudTask) {
                CrudTask.Create -> commonContainer.iApiItemQueryCreate(id, apiFilter)
                CrudTask.Read -> id?.let { commonContainer.iApiItemQueryRead(id, apiFilter) }
                CrudTask.Update -> id?.let { commonContainer.iApiItemQueryUpdate(id, apiFilter) }
                CrudTask.Delete -> id?.let { commonContainer.iApiItemQueryDelete(id, apiFilter) }
            }

            CallType.Action -> when (crudTask) {
                CrudTask.Create -> item?.let { commonContainer.iApiItemActionCreate(item, apiFilter) }
                CrudTask.Read -> null
                CrudTask.Update -> item?.let { commonContainer.iApiItemActionUpdate(item, apiFilter) }
                CrudTask.Delete -> item?.let { commonContainer.iApiItemActionDelete(item, apiFilter) }
            }
        } ?: return
        val paramList = listOf(
            Json.encodeToString(
                serializer = IApiItem.serializer(
                    commonContainer.itemSerializer,
                    commonContainer.idSerializer,
                    commonContainer.apiFilterSerializer
                ),
                value = iApiItem
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
                        ItemState.serializer(commonContainer.itemSerializer),
                        result
                    )
                block(itemResponse)
            } catch (e: Exception) {
                console.error(
                    "Error decoding KClass",
                    commonContainer.itemSerializer,
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
        configViewItemMap[this.baseUrl] = this
    }
}

@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, V : ViewItem<CC, T, ID, FILT>, AIS : IApiService, FILT : IApiFilter> configViewItem(
    viewFunc: KClass<out V>,
    serviceManager: KVServiceManager<AIS>,
    commonContainer: CC,
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    baseUrl: String? = null
): ConfigViewItem<CC, T, ID, V, AIS, FILT> = object : ConfigViewItem<CC, T, ID, V, AIS, FILT>(
    viewFunc = viewFunc,
    serviceManager = serviceManager,
    commonContainer = commonContainer,
    apiItemFun = apiItemFun,
    baseUrl = baseUrl
) {}
