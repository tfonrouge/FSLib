package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.config.ConfigViewItem.Companion.configViewItemMap
import com.fonrouge.fsLib.config.ConfigViewList
import com.fonrouge.fsLib.layout.NavbarTabulator
import com.fonrouge.fsLib.layout.TabulatorListContainer
import com.fonrouge.fsLib.layout.TabulatorMenuItem
import com.fonrouge.fsLib.layout.menuItem
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.CrudAction
import com.fonrouge.fsLib.model.IDataList
import com.fonrouge.fsLib.model.base.BaseModel
import io.kvision.core.Container
import io.kvision.tabulator.*
import io.kvision.toast.Toast
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

@Suppress("unused")
abstract class ViewList<T : BaseModel<U>, E : IDataList, U : Any>(
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
                "ViewItem${configView.itemKClass.js.name}"
            }
            return configViewItemMap[name]?.unsafeCast<ConfigViewItem<T, *, *, U>>()
        }

    var jsTabulatorBuilt: Boolean = false
    var masterViewItem: ViewItem<*, *>? = null
        set(value) {
            editable = value?.urlParams?.actionUpsert == true
            field = value
        }
    val parentContextUrlParams: String
        get() {
            return masterViewItem?.data?.value?.let {
                "&contextClass=${it::class.simpleName}&contextId=${it.item?._id}"
            } ?: ""
        }

    /**
     * Set to true if periodic update of table data is allowed
     */
    final override var periodicUpdateDataView: Boolean? = periodicUpdateDataView
        get() = field ?: KVWebManager.periodicUpdateDataViewList

    var tabulator: TabulatorListContainer<T, E, U>? = null
    var selectedIdList: List<Any?>? = null

    /**
     * Builds a string URL for the CRUD action and item provided
     *
     * @param crudAction [CrudAction] element
     * @param item the item list selected
     */
    fun actionUrl(crudAction: CrudAction, item: T?): String? {
        val urlParams = if (crudAction == CrudAction.Create) {
            UrlParams(
                "action" to CrudAction.Create.name
            )
        } else {
            item?.let {
                UrlParams(
                    "action" to crudAction.name,
                    "id" to encodedId(item)
                )
            }
        }
        masterViewItem?.let { viewItem ->
            urlParams?.addContext(viewItem.item, viewItem.encodedId())
        } ?: urlParams?.addContext(this@ViewList.urlParams?.contextDataUrl)
        return urlParams?.let {
            configViewItem?.let { it.url + urlParams.toString() }
        }
    }

    /**
     * On calling crud actions Create or Update on this list, checks if it has a masterViewItem
     * which is currently on Update action, if so, then performs an update call to back end before
     * calling the list crud action required
     */
    open fun checkIfmasterViewItemUpdate() {
        if (masterViewItem?.urlParams?.crudAction == CrudAction.Update) {
            masterViewItem?.acceptUpsertAction(block = null)
        }
    }

    /**
     * Creates an [UrlParams] with the 'contextClass' and 'contextId' values from
     * the [item] parameter provided.
     */
    fun urlContext(item: T?): UrlParams {
        return UrlParams().addContext(item = item, encodedId(item))
    }

    open fun MutableList<TabulatorMenuItem>.contextRowMenu(item: T?) {}

    fun contextRowMenuGenerator(): Array<TabulatorMenuItem>? {
        val item: T? = overItem?.let {
            try {
                tabulator?.toKotlinObjTabulator(it, configView.itemKClass)
            } catch (e: Exception) {
                Toast.error(e.message ?: "", "Error decoding (toKotlinObjTabulator)")
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
                    url = actionUrl(CrudAction.Read, item)
                )
                if (editable) {
                    menuItem(separator = true)
                    menuItem(
                        label = "Create",
                        icon = iconCrud(CrudAction.Create),
                        url = actionUrl(CrudAction.Create, item),
                        action = { e, c ->
                            checkIfmasterViewItemUpdate()
                        }
                    )
                    menuItem(
                        label = "Update",
                        icon = iconCrud(CrudAction.Update),
                        url = actionUrl(CrudAction.Update, item),
                        action = { e, c ->
                            checkIfmasterViewItemUpdate()
                        }
                    )
                    menuItem(
                        label = "Delete",
                        icon = iconCrud(CrudAction.Delete),
                        url = actionUrl(CrudAction.Delete, item)
                    )
                }
                contextRowMenu(item)
            }
            return menu.toTypedArray()
        }
        return null
    }

    override suspend fun dataUpdate() {
        if (jsTabulatorBuilt) {
            if (menuOpenedState != true) {
                selectedIdList = tabulator?.getSelectedData()?.map { it._id }
                tabulator?.apiCall()
            }
        }
    }

    override fun Container.displayPage() {
        pageBanner()
        pageListBody()
    }

    @OptIn(InternalSerializationApi::class)
    internal fun encodedId(item: T?): String {
        return item?.let {
            configView.idKClass?.let { Json.encodeToString(it.serializer(), item._id) }
        } ?: JSON.stringify(item?._id)
    }

    open fun onRowSelected(item: T?) {}

    abstract fun Container.pageListBody()

    fun updateLinks(item: T?, size: Int) {
        val id = item?._id
        navbarTabulator?.itemId = id
//        navbarTabulator?.linkCreate?.url = actionUrl(CrudAction.Create, id)
        navbarTabulator?.linkRead?.url = actionUrl(CrudAction.Read, item)
        navbarTabulator?.linkUpdate?.url = actionUrl(CrudAction.Update, item)
        navbarTabulator?.linkDelete?.url = actionUrl(CrudAction.Delete, item)
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
