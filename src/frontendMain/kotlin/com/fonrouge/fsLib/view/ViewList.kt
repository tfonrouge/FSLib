package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.config.ConfigViewItem.Companion.configViewItemMap
import com.fonrouge.fsLib.config.ConfigViewList
import com.fonrouge.fsLib.layout.NavbarTabulator
import com.fonrouge.fsLib.layout.TabulatorMenuItem
import com.fonrouge.fsLib.layout.menuItem
import com.fonrouge.fsLib.layout.update
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.CrudAction
import com.fonrouge.fsLib.model.IDataList
import com.fonrouge.fsLib.model.base.BaseModel
import io.kvision.core.Container
import io.kvision.state.ObservableList
import io.kvision.tabulator.ColumnDefinition
import io.kvision.tabulator.TabulatorRemote
import io.kvision.toast.Toast
import io.kvision.utils.toKotlinObj
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KClass

@Suppress("unused")
abstract class ViewList<T : BaseModel<U>, E : IDataList, U>(
    override val configView: ConfigViewList<T, out ViewList<T, E, U>, E, U>,
    configViewItem: ConfigViewItem<T, *, *, U>? = null,
    periodicUpdateDataView: Boolean? = null,
    editable: Boolean = true,
    icon: String? = null,
) : ViewDataContainer<List<T>>(
    configView = configView,
    editable = editable,
    icon = icon,
) {
    var jsonHelper: Json? = null
    var serializer: KSerializer<T>? = null
    var module: SerializersModule? = null

    /* dynamic content only used to get _id */
    var overItem: Any? = null
    var menuOpenedState: Boolean? = null
    var navbarTabulator: NavbarTabulator<U>? = null
    open val columnDefinitionList: List<ColumnDefinition<T>> = listOf()
    var configViewItem: ConfigViewItem<T, *, *, U>? = configViewItem
        get() {
            if (field != null) return field
            val viewClassName = configView.viewFunc.simpleName!!
            val name = if (viewClassName.contains("ViewList")) {
                viewClassName.replace("ViewList", "ViewItem")
            } else {
                "ViewItem${configView.klass.js.name}"
            }
            return configViewItemMap[name]?.unsafeCast<ConfigViewItem<T, *, *, U>>()
        }

    fun actionUrl(crudAction: CrudAction, itemId: U?): String? {
        val urlParams = if (crudAction == CrudAction.Create) {
            UrlParams(
                "action" to CrudAction.Create.name
            )
        } else {
            itemId?.let {
                UrlParams(
                    "action" to crudAction.name,
                    "id" to JSON.stringify(itemId)
                )
            }
        }
        masterViewItem?.let {
            urlParams?.addContext(it.dataContainer.value?.item)
        } ?: urlParams?.addContext(this@ViewList.urlParams?.contextDataUrl)
        masterViewItem?.callUpdateItemService()
        return urlParams?.let {
            configViewItem?.let { it.url + urlParams.toString() }
        }
    }

    /**
     * Observable that holds data list for the [ViewList]
     */
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

    /**
     * Set to true if periodic update of [dataContainer] is allowed
     */
    final override var periodicUpdateDataView: Boolean? = periodicUpdateDataView
        get() = field ?: KVWebManager.refreshViewListPeriodic
    var tabulator: TabulatorRemote<T, E>? = null
    var selectedIdList: List<Any?>? = null
    open fun MutableList<TabulatorMenuItem>.contextRowMenu(item: T?) {}

    fun contextRowMenuGenerator(): Array<TabulatorMenuItem>? {
        val item: T? = overItem?.let {
            try {
                dynamicToKotlinObj(it, configView.klass)
            } catch (e: Exception) {
                Toast.error(e.message ?: "", "Error decoding (toKotlinObj)")
                e.printStackTrace()
                null
            }
        }
        if (item != null) {
            val menu = mutableListOf<TabulatorMenuItem>()
            with(menu) {
                val labelId = configViewItem?.labelId?.invoke(item)
                menuItem(
                    label = " <font size=\"+1\">${configViewItem?.label}</font>: <b>$labelId</b>",
                    disabled = false,
                    header = true
                )
                menuItem(separator = true)
                menuItem(
                    label = "Detail of",
                    icon = iconCrud(CrudAction.Read),
                    url = actionUrl(CrudAction.Read, item._id)
                )
                if (editable) {
                    menuItem(separator = true)
                    menuItem(
                        label = "Create",
                        icon = iconCrud(CrudAction.Create),
                        url = actionUrl(CrudAction.Create, item._id)
                    )
                    menuItem(
                        label = "Update",
                        icon = iconCrud(CrudAction.Update),
                        url = actionUrl(CrudAction.Update, item._id)
                    )
                    menuItem(
                        label = "Delete",
                        icon = iconCrud(CrudAction.Delete),
                        url = actionUrl(CrudAction.Delete, item._id)
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

    override fun Container.displayPage() {
        pageBanner()
        pageListBody()
    }

    override suspend fun dataUpdate() {
        if (jsTabulatorBuilt) {
            if (menuOpenedState != true) {
                selectedIdList = tabulator?.getSelectedData()?.map { it._id }
                tabulator?.setPage(tabulator?.getPage() ?: 1)
            }
        }
    }

    fun dynamicToKotlinObj(data: dynamic, kClass: KClass<T>): T {
        if (data._children != null) {
            data._children =
                data._children.unsafeCast<Array<dynamic>>().map { dynamicToKotlinObj(it, kClass) }.toTypedArray()
        }
        return serializer?.let {
            jsonHelper?.decodeFromString(it, JSON.stringify(data))
        } ?: toKotlinObj(data, kClass)
    }

    fun updateLinks(item: T?, size: Int) {
        val id = item?._id
        navbarTabulator?.itemId = id
//        navbarTabulator?.linkCreate?.url = actionUrl(CrudAction.Create, id)
        navbarTabulator?.linkRead?.url = actionUrl(CrudAction.Read, id)
        navbarTabulator?.linkUpdate?.url = actionUrl(CrudAction.Update, id)
        navbarTabulator?.linkDelete?.url = actionUrl(CrudAction.Delete, id)
        if (id != null && size == 1) {
            navbarTabulator?.linkRead?.show()
            navbarTabulator?.linkUpdate?.show()
            navbarTabulator?.linkDelete?.show()
        } else {
            navbarTabulator?.linkRead?.hide()
            navbarTabulator?.linkUpdate?.hide()
            navbarTabulator?.linkDelete?.hide()
        }
    }
}
