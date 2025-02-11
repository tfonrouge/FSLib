package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.common.ICommonContainer
import com.fonrouge.fsLib.common.getItemState
import com.fonrouge.fsLib.commonServices.IApiCommonService
import com.fonrouge.fsLib.lib.toast
import com.fonrouge.fsLib.model.apiData.CallType
import com.fonrouge.fsLib.model.apiData.CrudTask
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.apiData.IApiItem
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import io.kvision.modal.Confirm
import io.kvision.modal.ModalSize
import io.kvision.remote.CallAgent
import io.kvision.remote.JsonRpcRequest
import io.kvision.remote.KVServiceManager
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import io.kvision.utils.Serialization
import kotlinx.coroutines.await
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromDynamic

/**
 * An abstract class representing a configuration data item that interacts with a specific type of API service.
 * This class provides the mechanism for handling CRUD tasks and their respective API calls, with detailed processing
 * for Query and Action types.
 *
 * @param CC The type of the common container used in this configuration data item, extending `ICommonContainer`.
 * @param T The type of the base document that this configuration data item is responsible for, extending `BaseDoc`.
 * @param ID The type of the identifier used by the base document.
 * @param FILT The type of API filter used for API calls, extending `IApiFilter`.
 * @param AIS The type of API service associated with this configuration data item, extending `IApiCommonService`.
 * @param commonContainer The common container instance associated with this configuration data item for handling context.
 * @param serviceManager Manages access to the API service and provides configuration or dependent services for API calls.
 * @param apiItemFun A higher-order function representing the API service function that is invoked for item state management.
 */
abstract class ConfigDataItem<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : IApiCommonService>(
    commonContainer: CC,
    private val serviceManager: KVServiceManager<AIS>,
    private val apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
) : ConfigData<CC, FILT>(commonContainer = commonContainer) {
    companion object {
        val configDataItemMap = mutableMapOf<String?, ConfigDataItem<*, *, *, *, *>>()
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
        callAgent.remoteCall(url = url, data = data, method = method).then { r: dynamic ->
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
                    text = "<b>Delete</b> '<i>${commonContainer.labelItem}</i>', id: <b>${
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

    suspend fun getItemState(id: ID, apiFilter: FILT = commonContainer.apiFilterInstance()): ItemState<T> =
        commonContainer.getItemState(serviceManager, apiItemFun, id, apiFilter) { it }.await()

    init {
        val name = commonContainer::class.simpleName?.let {
            if (it.startsWith("Common"))
                it.replaceFirst("Common", "ConfigDataItem")
            else
                "ConfigDataItem${commonContainer.itemKClass.js.name}"
        }
        configDataItemMap[name] = this
    }
}

/**
 * Configures a data item with a given common container, service manager, and API item function.
 *
 * @param CC The type of the common container, which must implement ICommonContainer.
 * @param T The type of the items managed by the common container, which must extend BaseDoc.
 * @param ID The type of the ID field of the items, which must be a non-nullable type.
 * @param FILT The type of the API filter used for querying, which must extend IApiFilter.
 * @param AIS The type of the API common service.
 * @param commonContainer An instance of the common container managing the data items.
 * @param serviceManager The service manager used for managing API services.
 * @param apiItemFun The suspend function executed on the API service, returning the state of the item.
 * @return A configured instance of ConfigDataItem with specific type parameters.
 */
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : IApiCommonService> configDataItem(
    commonContainer: CC,
    serviceManager: KVServiceManager<AIS>,
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>
): ConfigDataItem<CC, T, ID, FILT, AIS> = object : ConfigDataItem<CC, T, ID, FILT, AIS>(
    commonContainer = commonContainer,
    serviceManager = serviceManager,
    apiItemFun = apiItemFun
) {}