package com.fonrouge.fslib.view

import com.fonrouge.fslib.apiLib.IfceWebAction
import com.fonrouge.fslib.apiLib.KVWebManager
import com.fonrouge.fslib.apiLib.KVWebManager.configViewItemMap
import com.fonrouge.fslib.apiLib.KVWebManager.configViewListMap
import com.fonrouge.fslib.config.ConfigViewItem
import com.fonrouge.fslib.config.ConfigViewList
import com.fonrouge.fslib.layout.centeredMessage
import com.fonrouge.fslib.layout.update
import com.fonrouge.fslib.lib.ActionParam
import com.fonrouge.fslib.lib.UrlParams
import com.fonrouge.fslib.model.base.BaseContainerList
import com.fonrouge.fslib.model.base.BaseModel
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

@Suppress("unused")
abstract class ViewList<T : BaseModel, U : BaseContainerList<T>>(
    name: String,
    val listNameFunc: ((U) -> String) = { it.list?.getOrNull(0)?.id.toString() },
    val configViewList: ConfigViewList<ViewList<*, *>> = configViewListMap[name]!!,
    repeatRefreshView: Boolean? = null,
    loading: Boolean = false,
    editable: Boolean = true,
    icon: String? = null,
    actionPage: (View) -> IfceWebAction,
    matchFilterParam: JsonObject? = null,
    sortParam: JsonObject? = null,
) : ViewDataContainer<U>(
    name = name,
    configView = configViewList,
    loading = loading,
    editable = editable,
    icon = icon,
    actionPage = actionPage,
    restUrlParams = configViewList.restUrlParams,
    matchFilterParam = matchFilterParam,
    sortParam = sortParam
) {

    var blockRefresh: (() -> Unit)? = null

    val configViewItem: ConfigViewItem<*> by lazy { configViewItemMap[name]!! }

    override var repeatRefreshView: Boolean? = repeatRefreshView
        get() = field ?: KVWebManager.refreshViewListPeriodic

    var onUpdateContainerList: ((U?) -> Unit)? = null

    var tabulator: Tabulator<T>? = null

    var tabulatorDataSource: (() -> List<T>)? = null

    var masterViewItem: ViewItem<*, *>? = null

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
            field = value
            if (value?.listCRC32 != listCRC32) {
                listCRC32 = value?.listCRC32
                tabulator?.update(value?.list)
                onUpdateContainerList?.invoke(value)
                pageBannerLink?.let { onUpdatePageBannerLink?.invoke(it) }
            }
        }

    val actionParamMap = mapOf<ActionParam, (BaseModel?, (ViewItem<*, *>.() -> Unit)?) -> Unit>(
        ActionParam.Insert to { item, block ->
            val urlParams = UrlParams(
                "action" to ActionParam.Insert.name,
            )
            masterViewItem?.item?.let {
                urlParams.add("contextClass" to it::class.simpleName)
                urlParams.add("contextId" to it.id)
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
                    viewItem.configViewItem.updateData?.let { it1 -> it1(viewItem) }
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
            configViewList.updateData?.let { it(this) }
//            KVWebManager.dispatchPage(this)
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
