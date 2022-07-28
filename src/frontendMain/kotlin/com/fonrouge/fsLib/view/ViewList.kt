package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.StateItem
import com.fonrouge.fsLib.apiLib.KVWebManager
import com.fonrouge.fsLib.apiLib.KVWebManager.configViewItemMap
import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.config.ConfigViewList
import com.fonrouge.fsLib.layout.update
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.CrudAction
import com.fonrouge.fsLib.model.IDataList
import com.fonrouge.fsLib.model.base.BaseModel
import io.kvision.core.Container
import io.kvision.dropdown.ContextMenu
import io.kvision.html.Align
import io.kvision.modal.Confirm
import io.kvision.remote.KVServiceManager
import io.kvision.remote.RemoteData
import io.kvision.remote.RemoteFilter
import io.kvision.remote.RemoteSorter
import io.kvision.routing.routing
import io.kvision.state.ObservableList
import io.kvision.tabulator.ColumnDefinition
import io.kvision.tabulator.TabulatorRemote
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import kotlinx.serialization.json.JsonObject

@Suppress("unused")
abstract class ViewList<T : BaseModel<*>, E : IDataList>(
    override val configView: ConfigViewList<T, *>,
    val serverManager: KVServiceManager<E>,
    val function: suspend E.(Int?, Int?, List<RemoteFilter>?, List<RemoteSorter>?, String?) -> RemoteData<T>,
    repeatRefreshView: Boolean? = null,
    editable: Boolean = true,
    icon: String? = null,
    matchFilterParam: JsonObject? = null,
    sortParam: JsonObject? = null,
) : ViewDataContainer<List<T>>(
    configView = configView,
    editable = editable,
    icon = icon,
    restUrlParams = configView.restUrlParams,
    matchFilterParam = matchFilterParam,
    sortParam = sortParam
) {

    var blockRefresh: (() -> Unit)? = null
    open val columnDefinitionList: List<ColumnDefinition<T>> = listOf()
    val configViewItem: ConfigViewItem<*, *, *, Any>?
        get() {
            return configViewItemMap[name] as ConfigViewItem<*, *, *, Any>?
        }
    open val contextMenu: ((ContextMenu).() -> Unit)? = null
    val crudActionMap = mapOf<CrudAction, (Any?, (ViewItem<*, *>.() -> Unit)?) -> Unit>(
        CrudAction.Create to { _, _ ->
            configViewItem?.let { configViewItem ->
                val urlParams = UrlParams(
                    "action" to CrudAction.Create.name,
                )
                masterViewItem?.addContext(urlParams)
                masterViewItem?.callUpdateItemService()
                routing.navigate(configViewItem.url + urlParams.toString())
            }
        },
        CrudAction.Read to { itemId, block ->
            configViewItem?.let { configViewItem ->
                val urlParams = UrlParams(
                    "action" to CrudAction.Read.name,
                    "id" to itemId
                )
                masterViewItem?.addContext(urlParams)
                masterViewItem?.callUpdateItemService()
                routing.navigate(configViewItem.url + urlParams.toString())
            }
        },
        CrudAction.Update to { itemId, block ->
            configViewItem?.let { configViewItem ->
                itemId?.let {
                    val urlParams = UrlParams(
                        "action" to CrudAction.Update.name,
                        "id" to itemId,
                    )
                    masterViewItem?.addContext(urlParams)
                    masterViewItem?.callUpdateItemService()
                    routing.navigate(configViewItem.url + urlParams.toString())
                }
            }
        },
        CrudAction.Delete to { item, block ->
            item?.let {
                val itemConfigView = configViewItem
                Confirm.show(
                    caption = "Please confirm",
                    text = "Delete selected item: '${itemConfigView?.label}' ?",
                    align = Align.CENTER,
                    yesTitle = "Yes",
                    noTitle = "No",
                    centered = true
                ) {
                    configViewItem?.callItemService(
                        crudAction = CrudAction.Delete,
                        callType = StateItem.CallType.Action,
                        itemId = item
                    ) {
                        if (it.result) {
                            Toast.info("Item deleted", "Info")
                        } else {
                            Toast.warning(
                                message = "Item '${itemConfigView?.label}' id '${item}' not deleted",
                                title = "Warning",
                                options = ToastOptions(
                                    positionClass = ToastPosition.BOTTOMFULLWIDTH,
                                    progressBar = true
                                )
                            )
                        }
                    }
                }
            }
        }
    )
    var dataContainer: ObservableList<T>? = null
        set(value) {
            field = value
            pageBannerLink?.let { onUpdatePageBannerLink?.invoke(it) }
            tabulator?.update(dataContainer)
        }
    var jsTabulatorBuilt: Boolean = false
    val listNameFunc: ((List<T>) -> String) = { list ->
        list.getOrNull(0)?._id.toString()
    }
    var masterViewItem: ViewItem<*, *>? = null
    val parentContextUrlParams: String
        get() {
            return masterViewItem?.dataContainer?.value?.let {
                "&contextClass=${it::class.simpleName}&contextId=${it.item?._id}"
            } ?: ""
        }
    override var repeatUpdateView: Boolean? = repeatRefreshView
        get() = field ?: KVWebManager.refreshViewListPeriodic
    var tabulator: TabulatorRemote<T, E>? = null
    var updateDispatched = false

    override fun getName(): String? {
        return dataContainer?.let { listNameFunc.invoke(it) }
    }

    open fun onRowSelected(itemId: Any?) {}

    abstract fun Container.pageListBody()

    final override fun displayPage(container: Container) {
        container.apply {
            pageBanner()
            pageListBody()
        }
    }

    var selectedIdList: List<Any?>? = null

    override suspend fun singleUpdate() {
        if (jsTabulatorBuilt) {
            selectedIdList = tabulator?.getSelectedData()?.map { it._id }
            tabulator?.setPage(tabulator?.getPage() ?: 1)
        }
    }

    fun refreshList() {
        blockRefresh?.invoke()
    }
}
