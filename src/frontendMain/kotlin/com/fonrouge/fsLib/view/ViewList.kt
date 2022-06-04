package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.apiLib.KVWebManager
import com.fonrouge.fsLib.apiLib.KVWebManager.configViewItemMap
import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.config.ConfigViewList
import com.fonrouge.fsLib.layout.centeredMessage
import com.fonrouge.fsLib.layout.update
import com.fonrouge.fsLib.lib.ActionParam
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.IDataItem
import com.fonrouge.fsLib.model.IDataList
import com.fonrouge.fsLib.model.base.BaseModel
import io.kvision.core.Container
import io.kvision.dropdown.ContextMenu
import io.kvision.html.Align
import io.kvision.modal.Confirm
import io.kvision.modal.ModalSize
import io.kvision.remote.KVServiceManager
import io.kvision.remote.RemoteData
import io.kvision.remote.RemoteFilter
import io.kvision.remote.RemoteSorter
import io.kvision.state.ObservableList
import io.kvision.tabulator.ColumnDefinition
import io.kvision.tabulator.TabulatorRemote
import kotlinx.serialization.json.JsonObject
import kotlin.reflect.KProperty

@Suppress("unused")
abstract class ViewList<T : BaseModel<*>, E : IDataList>(
    override val configView: ConfigViewList<T, *>,
    val serverManager: KVServiceManager<E>,
    val function: suspend E.(Int?, Int?, List<RemoteFilter>?, List<RemoteSorter>?, String?) -> RemoteData<T>,
    repeatRefreshView: Boolean? = null,
    loading: Boolean = false,
    editable: Boolean = true,
    icon: String? = null,
    matchFilterParam: JsonObject? = null,
    sortParam: JsonObject? = null,
) : ViewDataContainer<List<T>>(
    configView = configView,
    loading = loading,
    editable = editable,
    icon = icon,
    restUrlParams = configView.restUrlParams,
    matchFilterParam = matchFilterParam,
    sortParam = sortParam
) {

    val listNameFunc: ((List<T>) -> String) = { list ->
        list.getOrNull(0)?._id.toString()
    }

    var blockRefresh: (() -> Unit)? = null

    val configViewItem: ConfigViewItem<*, *>? by lazy { configViewItemMap[name] }

    override var repeatRefreshView: Boolean? = repeatRefreshView
        get() = field ?: KVWebManager.refreshViewListPeriodic

    var tabulator: TabulatorRemote<T, E>? = null

    var masterViewItem: ViewItem<*, IDataItem>? = null
    var masterItemProp: KProperty<*>? = null

    val parentContextUrlParams: String
        get() {
            return masterViewItem?.dataContainer?.value?.let {
                "&contextClass=${it::class.simpleName}&contextId=${it.item?._id}"
            } ?: ""
        }

    open val contextMenu: ((ContextMenu).() -> Unit)? = null

    var dataContainer: ObservableList<T>? = null
        set(value) {
            field = value
            pageBannerLink?.let { onUpdatePageBannerLink?.invoke(it) }
            tabulator?.update(dataContainer)
        }

    val actionParamMap = mapOf<ActionParam, (BaseModel<*>?, (ViewItem<*, *>.() -> Unit)?) -> Unit>(
        ActionParam.Insert to { item, block ->
            val urlParams = UrlParams(
                "action" to ActionParam.Insert.name,
            )
            masterViewItem?.dataContainer?.value?.let {
                urlParams.add("contextClass" to it::class.simpleName)
                urlParams.add("contextId" to it.item?._id)
                urlParams.add("contextName" to masterItemProp?.name)
            }
            configViewItem?.viewFunc?.let { it(urlParams) }?.let { viewItem ->
                block?.let { block.invoke(viewItem) }
                viewItem.displayModal(caption = "Inserting this...", size = ModalSize.XLARGE, centered = true)
            }
        },
        ActionParam.Update to { item, block ->
            item?.let {
                val urlParams = UrlParams(
                    "action" to ActionParam.Update.name,
                    "id" to item._id,
                )
                masterViewItem?.dataContainer?.value?.let {
                    urlParams.add("contextClass" to it::class.simpleName)
                    urlParams.add("contextId" to it.item?._id)
                    urlParams.add("contextName" to masterItemProp?.name)
                }
                configViewItem?.displayModal(urlParams, block)
            }
        },
        ActionParam.Delete to { item, block ->
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
/*
                    KVWebManager.deleteItem(item) {
                        if (it == true) {
                            Toast.info("Item deleted", "Info")
                        } else {
                            Toast.warning(
                                message = "Item '${itemConfigView?.label}' id '${item.id}' not deleted",
                                title = "Warning",
                                options = ToastOptions(
                                    positionClass = ToastPosition.BOTTOMFULLWIDTH,
                                    progressBar = true
                                )
                            )
                        }
                    }
*/
                }
            }
        }
    )

    override fun getName(): String? {
        return dataContainer?.let { listNameFunc.invoke(it) }
    }

    open fun onRowSelected(item: T?) {}

    open val columnDefinitionList: List<ColumnDefinition<T>> = listOf()

    abstract fun pageListBody(container: Container)

    var updateDispatched = false

    final override fun displayPage(container: Container) {

        this.container = container

        container.apply {
            pageBanner()
            pageListBody(this)
        }
    }

    fun refreshList() {
        blockRefresh?.invoke()
    }
}
