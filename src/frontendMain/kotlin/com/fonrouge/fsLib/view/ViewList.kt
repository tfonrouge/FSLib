package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.apiLib.KVWebManager
import com.fonrouge.fsLib.apiLib.KVWebManager.configViewItemMap
import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.config.ConfigViewList
import com.fonrouge.fsLib.layout.centeredMessage
import com.fonrouge.fsLib.layout.update
import com.fonrouge.fsLib.lib.ActionParam
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.base.BaseModel
import io.kvision.core.Container
import io.kvision.dropdown.ContextMenu
import io.kvision.html.Align
import io.kvision.modal.Confirm
import io.kvision.modal.ModalSize
import io.kvision.tabulator.ColumnDefinition
import io.kvision.tabulator.Tabulator
import kotlinx.serialization.json.JsonObject
import kotlin.reflect.KProperty

@Suppress("unused")
abstract class ViewList<T : BaseModel<*>>(
    val configViewList: ConfigViewList<*, *>,
    repeatRefreshView: Boolean? = null,
    loading: Boolean = false,
    editable: Boolean = true,
    icon: String? = null,
//    actionPage: (View) -> IfceWebAction,
    matchFilterParam: JsonObject? = null,
    sortParam: JsonObject? = null,
) : ViewDataContainer<List<T>>(
    configView = configViewList,
    loading = loading,
    editable = editable,
    icon = icon,
//    actionPage = actionPage,
    restUrlParams = configViewList.restUrlParams,
    matchFilterParam = matchFilterParam,
    sortParam = sortParam
) {

    val listNameFunc: ((List<T>) -> String) = { list ->
        list.getOrNull(0)?.id.toString()
    }

    var blockRefresh: (() -> Unit)? = null

    val configViewItem: ConfigViewItem<*, *>? by lazy { configViewItemMap[name] }

    override var repeatRefreshView: Boolean? = repeatRefreshView
        get() = field ?: KVWebManager.refreshViewListPeriodic

    var onUpdateContainerList: ((List<T>?) -> Unit)? = null

    var tabulator: Tabulator<T>? = null

    var tabulatorDataSource: (() -> List<T>)? = null

    var masterViewItem: ViewItem<*>? = null
    var masterItemProp: KProperty<*>? = null

    val parentContextUrlParams: String
        get() {
            return masterViewItem?.item?.let {
                "&contextClass=${it::class.simpleName}&contextId=${it.id}"
            } ?: ""
        }

    open val contextMenu: ((ContextMenu).() -> Unit)? = null

    override var dataContainer: List<T>? = null
        set(value) {
            field = value
            onUpdateContainerList?.invoke(value)
            pageBannerLink?.let { onUpdatePageBannerLink?.invoke(it) }
            console.warn("assigning tabulator ", objId, tabulator, dataContainer)
            tabulator?.update(dataContainer)
        }

    val actionParamMap = mapOf<ActionParam, (BaseModel<*>?, (ViewItem<*>.() -> Unit)?) -> Unit>(
        ActionParam.Insert to { item, block ->
            val urlParams = UrlParams(
                "action" to ActionParam.Insert.name,
            )
            masterViewItem?.item?.let {
                urlParams.add("contextClass" to it::class.simpleName)
                urlParams.add("contextId" to it.id)
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
                    "id" to item.id,
                )
                masterViewItem?.item?.let {
                    urlParams.add("contextClass" to it::class.simpleName)
                    urlParams.add("contextId" to it.id)
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

    fun dispatchPage(container: Container) {
        displayPage(container)
        if (!updateDispatched) {
            updateDispatched = true
//            configView?.updateData()
            updateData()
//            configViewList.updateData(this as Nothing)
        }
    }

    final override fun displayPage(container: Container) {

        this.container = container

        container.apply {
            pageBanner(this@ViewList)
            if (loading) {
                centeredMessage("loading...")
            } else {
                console.warn("calling pageListBody()")
                pageListBody(this)
            }
        }
    }

    fun refreshList() {
        skipLoading = true
        blockRefresh?.invoke()
    }
}
