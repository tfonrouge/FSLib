package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.ContextDataUrl
import com.fonrouge.fsLib.StateItem
import com.fonrouge.fsLib.apiLib.KVWebManager
import com.fonrouge.fsLib.apiLib.TypeView
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.CrudAction
import com.fonrouge.fsLib.model.IDataItem
import com.fonrouge.fsLib.model.ItemContainer
import com.fonrouge.fsLib.model.base.BaseModel
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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromDynamic
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

abstract class ConfigViewItem<T : BaseModel<U>, V : ViewItem<T, U>, E : IDataItem, U>(
    private val klass: KClass<T>,
    label: String,
    viewFunc: ((UrlParams?) -> V),
    restUrlParams: UrlParams? = null,
    lookupParam: JsonObject? = null,
    private val serverManager: KVServiceManager<E>,
    private val function: suspend E.(U?, StateItem<T>) -> ItemContainer<T>,
    private val stateFunction: (() -> String)? = null,
) : ConfigViewContainer<T, V>(
    name = klass.simpleName!!,
    label = label,
    restUrlParams = restUrlParams,
    lookupParam = lookupParam,
    typeView = TypeView.Item,
    viewFunc = viewFunc,
) {

    val labelDelete = "Delete $label"
    val labelDetail = "Detail of $label"
    val labelCreate = "Create $label"
    val labelUpdate = "Update $label"

    fun labelUrlRead(id: U) = label to urlRead(id)
    fun labelUrlUpdate(id: U) = label to urlUpdate(id)

    val urlCreate: String
        get() {
            val urlParams = UrlParams("action" to CrudAction.Create.name)
            return navigoUrl + urlParams.toString()
        }

    fun urlRead(id: U): String {
        val urlParams = UrlParams("id" to JSON.stringify(id), "action" to CrudAction.Read.name)
        return navigoUrl + urlParams.toString()
    }

    fun urlDelete(id: U): String {
        val urlParams = UrlParams("id" to JSON.stringify(id), "action" to CrudAction.Delete.name)
        return navigoUrl + urlParams.toString()
    }

    fun urlUpdate(id: U): String {
        val urlParams = UrlParams("id" to JSON.stringify(id), "action" to CrudAction.Update.name)
        return navigoUrl + urlParams.toString()
    }

    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    fun callItemService(
        crudAction: CrudAction,
        callType: StateItem.CallType,
        itemId: String? = JSON.stringify(null),
        item: T? = null,
        contextDataUrl: ContextDataUrl? = null,
        block: (ItemContainer<T>) -> Unit,
    ) {
        val (url, method) = serverManager.requireCall(function)
        val callAgent = CallAgent()
        val paramList = listOf(
            itemId,
            Json.encodeToString(
                serializer = StateItem.serializer(klass.serializer()),
                value = StateItem(
                    item = item,
                    json = null,
                    crudAction = crudAction,
                    callType = callType,
                    state = stateFunction?.invoke(),
                    contextDataUrl = contextDataUrl
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
            val itemContainer: ItemContainer<T> =
                Json.decodeFromDynamic(ItemContainer.serializer(klass.serializer()), result)
            block(itemContainer)
        }
    }

    init {
        KVWebManager.configViewItemMap[name] = this
    }
}
