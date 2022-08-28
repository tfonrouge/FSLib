package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.StateItem
import com.fonrouge.fsLib.apiLib.KVWebManager
import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.config.ConfigViewItem.Companion.configViewItemMap
import com.fonrouge.fsLib.config.ConfigViewList
import com.fonrouge.fsLib.layout.TabulatorMenuItem
import com.fonrouge.fsLib.layout.menuItem
import com.fonrouge.fsLib.layout.update
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.CrudAction
import com.fonrouge.fsLib.model.IDataList
import com.fonrouge.fsLib.model.base.BaseModel
import io.kvision.core.Container
import io.kvision.html.Align
import io.kvision.modal.Confirm
import io.kvision.routing.routing
import io.kvision.state.ObservableList
import io.kvision.tabulator.ColumnDefinition
import io.kvision.tabulator.TabulatorRemote
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import io.kvision.utils.toKotlinObj

@Suppress("unused")
abstract class ViewList<T : BaseModel<U>, E : IDataList, U>(
    override val configView: ConfigViewList<T, out ViewList<T, E, U>, E, U>,
    repeatRefreshView: Boolean? = null,
    editable: Boolean = true,
    icon: String? = null,
) : ViewDataContainer<List<T>>(
    configView = configView,
    editable = editable,
    icon = icon,
) {
    /* dynamic content only used to get _id */
    var overItem: Any? = null
    var menuState: RowContextMenuState = RowContextMenuState.Unknown
    open val columnDefinitionList: List<ColumnDefinition<T>> = listOf()
    val configViewItem: ConfigViewItem<T, *, *, U>?
        get() {
            return configViewItemMap[name]?.unsafeCast<ConfigViewItem<T, *, *, U>>()
        }

    val crudActionMap = mapOf<CrudAction, (U?) -> Unit>(
        CrudAction.Create to {
            configViewItem?.let { configViewItem ->
                val urlParams = UrlParams(
                    "action" to CrudAction.Create.name,
                )
                masterViewItem?.addContext(urlParams)
                masterViewItem?.callUpdateItemService()
                routing.navigate(configViewItem.url + urlParams.toString())
            }
        },
        CrudAction.Read to { itemId ->
            configViewItem?.let { configViewItem ->
                val urlParams = UrlParams(
                    "action" to CrudAction.Read.name,
                    "id" to JSON.stringify(itemId)
                )
                masterViewItem?.addContext(urlParams)
                masterViewItem?.callUpdateItemService()
                routing.navigate(configViewItem.url + urlParams.toString())
            }
        },
        CrudAction.Update to { itemId ->
            configViewItem?.let { configViewItem ->
                itemId?.let {
                    val urlParams = UrlParams(
                        "action" to CrudAction.Update.name,
                        "id" to JSON.stringify(itemId),
                    )
                    masterViewItem?.addContext(urlParams)
                    masterViewItem?.callUpdateItemService()
                    routing.navigate(configViewItem.url + urlParams.toString())
                }
            }
        },
        CrudAction.Delete to { itemId ->
            itemId?.let {
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
                        itemId = JSON.stringify(itemId)
                    ) {
                        if (it.result) {
                            Toast.info("Item deleted", "Info")
                        } else {
                            Toast.warning(
                                message = "Item '${itemConfigView?.label}' id '${itemId}' not deleted",
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
    var masterViewItem: ViewItem<*, *>? = null
        set(value) {
            editable = value?.urlParams?.actionUpsert == true
            field = value
        }
    val parentContextUrlParams: String
        get() {
            return masterViewItem?.dataContainer?.value?.let {
                "&contextClass=${it::class.simpleName}&contextId=${it.item?._id}"
            } ?: ""
        }
    override var repeatUpdateView: Boolean? = repeatRefreshView
        get() = field ?: KVWebManager.refreshViewListPeriodic
    var tabulator: TabulatorRemote<T, E>? = null
    var selectedIdList: List<Any?>? = null
    open fun MutableList<TabulatorMenuItem>.contextRowMenu(item: T?) {}

    fun contextRowMenuGenerator(): Array<TabulatorMenuItem>? {
        val item: T? = overItem?.let { toKotlinObj(it, configView.klass) }
        if (item != null) {
            val menu = mutableListOf<TabulatorMenuItem>()
            with(menu) {
                val labelId = configViewItem?.labelId?.invoke(item)
                menuItem(label = "${configViewItem?.label}: $labelId", disabled = true)
                menuItem(separator = true)
                menuItem(
                    label = "Detail of",
                    icon = "fas fa-eye",
                    url = configViewItem?.urlRead(item._id)
                )
                if (editable) {
                    menuItem(separator = true)
                    menuItem(
                        label = "Create",
                        icon = "fas fa-plus",
                        url = configViewItem?.urlCreate
                    )
                    menuItem(
                        label = "Update",
                        icon = "fas fa-edit",
                    )
                    menuItem(
                        label = "Delete",
                        icon = "fas fa-trash-alt",
                    )
                }
                contextRowMenu(item)
            }
            return menu.toTypedArray()
        }
        return null
    }

    open fun onRowSelected(item: T?) {}

    abstract fun Container.pageListBody()

    final override fun Container.displayPage() {
        pageBanner()
        pageListBody()
    }

    override suspend fun dataUpdate() {
        if (jsTabulatorBuilt) {
            if (menuState != RowContextMenuState.Opened) {
                selectedIdList = tabulator?.getSelectedData()?.map { it._id }
                tabulator?.setPage(tabulator?.getPage() ?: 1)
            }
        }
    }

    enum class RowContextMenuState {
        Unknown,
        Opened,
        Closed,
    }
}
