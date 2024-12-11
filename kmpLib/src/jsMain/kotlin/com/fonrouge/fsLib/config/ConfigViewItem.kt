package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.commonServices.IApiCommonService
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.lib.toEncodedUrlString
import com.fonrouge.fsLib.lib.toast
import com.fonrouge.fsLib.model.apiData.*
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import com.fonrouge.fsLib.view.ViewItem
import io.kvision.modal.Confirm
import io.kvision.modal.ModalSize
import io.kvision.remote.CallAgent
import io.kvision.remote.HttpMethod
import io.kvision.remote.JsonRpcRequest
import io.kvision.remote.KVServiceManager
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import io.kvision.utils.Serialization
import kotlinx.browser.window
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromDynamic
import org.w3c.dom.Window
import kotlin.reflect.KClass

/**
 * Abstract class representing a configuration view item that supports various CRUD operations
 * and interacts with services to perform these actions.
 *
 * @param CC Type parameter representing a common container that implements ICommonContainer.
 * @param T Type parameter representing a base document that inherits from BaseDoc.
 * @param ID Type parameter representing the ID of the base document.
 * @param V Type parameter representing the view item that this configuration view item corresponds to.
 * @param AIS Type parameter representing the API common service that aids in performing CRUD operations.
 * @param FILT Type parameter representing the API filter used in service calls.
 * @param serviceManager Manager that handles services of the type AIS.
 * @param commonContainer Common container that holds items of the type T.
 * @param apiItemFun Suspend function representing the API item and its state.
 * @param viewKClass KClass of the view item.
 * @param baseUrl Optional base URL for the configuration view item.
 */
abstract class ConfigViewItem<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, V : ViewItem<CC, T, ID, FILT>, AIS : IApiCommonService, FILT : IApiFilter<*>>(
    private val serviceManager: KVServiceManager<AIS>,
    override val commonContainer: CC,
    private val apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    viewKClass: KClass<out V>,
    baseUrl: String? = null
) : ConfigViewContainer<CC, T, ID, V, FILT>(
    viewKClass = viewKClass,
    commonContainer = commonContainer,
    baseUrl = baseUrl,
) {
    override val baseUrl: String
        get() {
            return _baseUrl ?: viewKClass.simpleName!!
        }

    companion object {
        val configViewItemMap = mutableMapOf<String, ConfigViewItem<*, *, *, *, *, *>>()
    }

    override val label: String get() = commonContainer.labelItem

    override val labelUrl: Pair<String, String> by lazy { commonContainer.labelItem to url }

    @Suppress("unused")
    fun labelUrlRead(id: ID) = commonContainer.labelItem to urlRead(id)

    @Suppress("unused")
    fun labelUrlUpdate(id: ID) = commonContainer.labelItem to urlUpdate(id)

    val urlCreate: String
        get() {
            val urlParams = UrlParams("action" to CrudTask.Create.name)
            return url + urlParams.toEncodedUrlString()
        }

    /**
     * Confirms deletion of a specific item by presenting a modal dialog to the user.
     * If the user confirms the action, the item is deleted via a service call.
     * Handles success and failure cases with appropriate callbacks.
     *
     * @param item The item to be deleted.
     * @param apiFilter The API filter used for the deletion call. Defaults to an instance from the common container.
     * @param onFail An optional callback invoked when the delete operation fails. Receives the item state as a parameter.
     * @param onSuccess An optional callback invoked when the delete operation is successful.
     */
    fun confirmDeleteView(
        item: T,
        apiFilter: FILT = commonContainer.apiFilterInstance(),
        onFail: ((ItemState<T>) -> Unit)? = null,
        onSuccess: (() -> Unit)? = null,
    ) {
        callItemService(
            crudTask = CrudTask.Delete,
            callType = CallType.Query,
            id = item._id,
            item = item,
            apiFilter = apiFilter,
        ) { itemState ->
            if (itemState.hasError.not()) {
                val modal = Confirm(
                    caption = "Please Confirm",
                    text = "<b>Delete</b> '<i>${label}</i>', id: <b>${
                        commonContainer.labelIdFunc(item)
                    }</b> ?",
                    rich = true,
                    size = ModalSize.XLARGE,
                    centered = true,
                    noTitle = "Cancel",
                    noCallback = {
                        Toast.warning("Delete canceled")
                    },
                    yesCallback = {
                        callItemService(
                            crudTask = CrudTask.Delete,
                            callType = CallType.Action,
                            id = item._id,
                            item = item,
                            apiFilter = apiFilter,
                        ) { itemState1 ->
                            if (itemState1.hasError.not()) {
                                Toast.success(
                                    message = itemState1.msgOk ?: "Delete action successful ...",
                                )
                                onSuccess?.invoke()
                            } else {
                                Toast.warning(
                                    message = itemState1.msgError ?: "Delete action failed ...",
                                )
                                onFail?.invoke(itemState1)
                            }
                            itemState1
                        }
                    }
                )
                modal.show()
            } else {
                itemState.toast()
                onFail?.invoke(itemState)
            }
            itemState
        }
    }

    /**
     * Opens a new browser window or tab with the URL corresponding to the given ApiItem.
     *
     * @param apiItem The ApiItem representing the query to be executed.
     * @param target The target where the URL should be opened. Defaults to "_blank".
     * @return The window object of the newly opened window/tab, or null if the URL cannot be generated.
     */
    @Suppress("unused")
    fun navigateTo(apiItem: ApiItem<T, ID, FILT>, target: String = "_blank"): Window? {
        return viewItemUrl(apiItem)?.let { url ->
            window.open(
                url = url,
                target = target
            )
        }
    }

    fun urlRead(id: ID): String {
        val urlParams =
            UrlParams(
                "id" to Json.encodeToString(commonContainer.idSerializer, id),
                "action" to CrudTask.Read.name
            )
        return url + urlParams.toEncodedUrlString()
    }

    @Suppress("unused")
    fun urlDelete(id: ID): String {
        val urlParams =
            UrlParams(
                "id" to Json.encodeToString(commonContainer.idSerializer, id),
                "action" to CrudTask.Delete.name
            )
        return url + urlParams.toEncodedUrlString()
    }

    fun urlUpdate(id: ID): String {
        val urlParams =
            UrlParams(
                "id" to Json.encodeToString(commonContainer.idSerializer, id),
                "action" to CrudTask.Update.name
            )
        return url + urlParams.toEncodedUrlString()
    }

    fun viewItemUrl(apiItem: ApiItem<T, ID, FILT>): String? {
        val url: String? = when (apiItem) {
            is ApiItem.Upsert.Create.Query -> listOf("action" to CrudTask.Create.name)
            is ApiItem.Read -> listOf(
                "action" to apiItem.crudTask.name,
                "id" to Json.encodeToString(commonContainer.idSerializer, apiItem.id)
            )

            is ApiItem.Upsert.Update.Query -> listOf(
                "action" to apiItem.crudTask.name,
                "id" to Json.encodeToString(commonContainer.idSerializer, apiItem.id)
            )

            is ApiItem.Delete.Query -> listOf(
                "action" to apiItem.crudTask.name,
                "id" to Json.encodeToString(commonContainer.idSerializer, apiItem.id)
            )

            else -> null
        }?.let { params: List<Pair<String, String>> ->
            val urlParams = UrlParams(*params.toTypedArray())
            urlParams.pushParam(
                "apiFilter" to Json.encodeToString(
                    commonContainer.apiFilterSerializer,
                    apiItem.apiFilter
                )
            )
            url + urlParams.toEncodedUrlString()
        }
        return url
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun callItemService(
        crudTask: CrudTask,
        callType: CallType,
        id: ID? = null,
        item: T? = null,
        orig: T? = null,
        apiFilter: FILT = commonContainer.apiFilterInstance(),
        block: (ItemState<T>) -> ItemState<T>,
    ) {
        val (url, method) = serviceManager.requireCall(apiItemFun)
        val callAgent = CallAgent()
        val iApiItem = when (callType) {
            CallType.Query -> when (crudTask) {
                CrudTask.Create -> commonContainer.iApiItemQueryCreate(apiFilter)
                CrudTask.Read -> id?.let { commonContainer.iApiItemQueryRead(id, apiFilter) }
                CrudTask.Update -> id?.let { commonContainer.iApiItemQueryUpdate(id, apiFilter) }
                CrudTask.Delete -> id?.let { commonContainer.iApiItemQueryDelete(id, apiFilter) }
            }

            CallType.Action -> when (crudTask) {
                CrudTask.Create -> item?.let {
                    commonContainer.iApiItemActionCreate(
                        item,
                        apiFilter
                    )
                }

                CrudTask.Read -> null
                CrudTask.Update -> item?.let {
                    commonContainer.iApiItemActionUpdate(
                        item,
                        apiFilter,
                        orig
                    )
                }

                CrudTask.Delete -> item?.let {
                    commonContainer.iApiItemActionDelete(
                        item,
                        apiFilter
                    )
                }
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
        callAgent.remoteCall(url, data, method = HttpMethod.valueOf(method.name))
            .then { r: dynamic ->
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
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, V : ViewItem<CC, T, ID, FILT>, AIS : IApiCommonService, FILT : IApiFilter<*>> configViewItem(
    viewKClass: KClass<out V>,
    serviceManager: KVServiceManager<AIS>,
    commonContainer: CC,
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    baseUrl: String? = null
): ConfigViewItem<CC, T, ID, V, AIS, FILT> = object : ConfigViewItem<CC, T, ID, V, AIS, FILT>(
    viewKClass = viewKClass,
    serviceManager = serviceManager,
    commonContainer = commonContainer,
    apiItemFun = apiItemFun,
    baseUrl = baseUrl
) {}
