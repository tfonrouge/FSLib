package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.apiLib.KVWebManager
import com.fonrouge.fsLib.apiLib.KVWebManager.configViewItemMap
import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.config.ConfigViewList
import com.fonrouge.fsLib.layout.centeredMessage
import com.fonrouge.fsLib.layout.update
import com.fonrouge.fsLib.lib.ActionParam
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.base.BaseContainerList
import com.fonrouge.fsLib.model.base.BaseModel
import io.kvision.core.Container
import io.kvision.dropdown.ContextMenu
import io.kvision.html.Align
import io.kvision.modal.Confirm
import io.kvision.modal.ModalSize
import io.kvision.tabulator.ColumnDefinition
import io.kvision.tabulator.Tabulator
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import kotlinx.serialization.json.JsonObject
import kotlin.reflect.KProperty

@Suppress("unused")
abstract class ViewList<T : BaseModel<*>, U : BaseContainerList<T>>(
    val listNameFunc: ((U) -> String) = { it.list.getOrNull(0)?.id.toString() },
    val configViewList: ConfigViewList<*, *, *>,
    repeatRefreshView: Boolean? = null,
    loading: Boolean = false,
    editable: Boolean = true,
    icon: String? = null,
//    actionPage: (View) -> IfceWebAction,
    matchFilterParam: JsonObject? = null,
    sortParam: JsonObject? = null,
) : ViewDataContainer<U>(
    configView = configViewList,
    loading = loading,
    editable = editable,
    icon = icon,
//    actionPage = actionPage,
    restUrlParams = configViewList.restUrlParams,
    matchFilterParam = matchFilterParam,
    sortParam = sortParam
) {

    var blockRefresh: (() -> Unit)? = null

    val configViewItem: ConfigViewItem<*, *, *> by lazy { configViewItemMap[name]!! }

    override var repeatRefreshView: Boolean? = repeatRefreshView
        get() = field ?: KVWebManager.refreshViewListPeriodic

    var onUpdateContainerList: ((U?) -> Unit)? = null

    var tabulator: Tabulator<T>? = null

    var tabulatorDataSource: (() -> List<T>)? = null

    var masterViewItem: ViewItem<*, *>? = null
    var masterItemProp: KProperty<*>? = null

    var listCRC32: String? = null

    val parentContextUrlParams: String
        get() {
            return masterViewItem?.item?.let {
                "&contextClass=${it::class.simpleName}&contextId=${it.id}"
            } ?: ""
        }

    open val contextMenu: ((ContextMenu).() -> Unit)? = null

    override var dataContainer: U? = null
        set(value) {
            console.warn("dataContainer...", value)
            field = value
            onUpdateContainerList?.invoke(value)
            pageBannerLink?.let { onUpdatePageBannerLink?.invoke(it) }
            console.warn("CRC32 dataContainer...", value?.listCRC32, listCRC32)
            if (value?.listCRC32 != listCRC32) {
                console.warn("assigning dataContainer...", value)
                listCRC32 = value?.listCRC32
                tabulator?.update(value?.list?.toList())
            }
        }

    val actionParamMap = mapOf<ActionParam, (BaseModel<*>?, (ViewItem<*, *>.() -> Unit)?) -> Unit>(
        ActionParam.Insert to { item, block ->
            val urlParams = UrlParams(
                "action" to ActionParam.Insert.name,
            )
            masterViewItem?.item?.let {
                urlParams.add("contextClass" to it::class.simpleName)
                urlParams.add("contextId" to it.id)
                urlParams.add("contextName" to masterItemProp?.name)
            }
            configViewItem.viewFunc?.let { it(urlParams) }?.let { viewItem: ViewItem<*, *> ->
                block?.let { block.invoke(viewItem) }
                viewItem.displayModal(caption = "Inserting this...", size = ModalSize.XLARGE, centered = true)
            }
        },
        ActionParam.Update to { item, block ->
            item?.let {
                val urlParams = UrlParams(
                    "action" to ActionParam.Update.name,
                    "id" to item.id,
                )
                masterViewItem?.item?.let {
                    urlParams.add("contextClass" to it::class.simpleName)
                    urlParams.add("contextId" to it.id)
                    urlParams.add("contextName" to masterItemProp?.name)
                }
                configViewItem.viewFunc?.let { it1 -> it1(urlParams) }?.let { viewItem: ViewItem<*, *> ->
                    block?.let { block.invoke(viewItem) }
                    viewItem.displayBlock = {
                        viewItem.displayModal(
                            caption = "Updating this...",
                            size = ModalSize.XLARGE,
                            centered = true
                        )
                    }
//                    viewItem.configViewItem.updateData.let { it1 -> it1(viewItem) }
                    viewItem.configViewItem.updateData(urlParams)
                }
            }
        },
        ActionParam.Delete to { item, block ->
            item?.let {
                val itemConfigView = configViewItem
                Confirm.show(
                    caption = "Please confirm",
                    text = "Delete selected item: '${itemConfigView.label}' ?",
                    align = Align.CENTER,
                    yesTitle = "Yes",
                    noTitle = "No",
                    centered = true
                ) {
                    KVWebManager.deleteItem(item) {
                        if (it == true) {
                            Toast.info("Item deleted", "Info")
                        } else {
                            Toast.warning(
                                message = "Item '${itemConfigView.label}' id '${item.id}' not deleted",
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

    override fun getName(): String? {
        return dataContainer?.let { listNameFunc.invoke(it) }
    }

    open fun onRowSelected(item: T?) {}

    open val columnDefinitionList: List<ColumnDefinition<T>> = listOf()

    abstract fun pageListBody(container: Container)

    var updateDispatched = false

    fun dispatchPage(container: Container) {
        displayPage(container)
        if (!updateDispatched) {
            updateDispatched = true
            configViewList.updateData(urlParams = urlParams)
        }
    }

    final override fun displayPage(container: Container) {

        this.container = container

        container.apply {
            pageBanner(this@ViewList)
            if (loading) {
                centeredMessage("loading...")
            } else {
                pageListBody(this)
            }
        }
    }

    fun refreshList() {
        skipLoading = true
        blockRefresh?.invoke()
    }
}
