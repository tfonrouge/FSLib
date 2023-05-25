package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.config.ConfigViewItem.Companion.configViewItemMap
import com.fonrouge.fsLib.config.ConfigViewList
import com.fonrouge.fsLib.layout.NavbarTabulator
import com.fonrouge.fsLib.layout.TabulatorListContainer
import com.fonrouge.fsLib.layout.TabulatorMenuItem
import com.fonrouge.fsLib.layout.menuItem
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.lib.iconCrud
import com.fonrouge.fsLib.model.CrudTask
import com.fonrouge.fsLib.model.IDataList
import com.fonrouge.fsLib.model.base.BaseDoc
import io.kvision.core.Container
import io.kvision.offcanvas.Offcanvas
import io.kvision.state.ObservableValue
import io.kvision.tabulator.ColumnDefinition
import io.kvision.toast.Toast
import kotlinx.browser.window
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer

@Suppress("unused")
abstract class ViewList<T : BaseDoc<U>, E : IDataList, U : Any, F : Any>(
    override val configView: ConfigViewList<T, out ViewList<T, E, U, F>, E, U, F>,
    configViewItem: ConfigViewItem<T, *, *, U>? = null,
    periodicUpdateDataView: Boolean? = null,
    editable: Boolean = true,
    icon: String? = null,
    /**
     * If apiFilter kclass is not defined in [configView] this value needs to be initialized
     * on view construct in order to automatically get [apiFilter] parameter from url params
     */
    apiFilter: F? = null,
) : ViewDataContainer<List<T>>(
    configView = configView,
    editable = editable,
    icon = icon,
) {
    val apiFilter: ObservableValue<F?> = ObservableValue(apiFilter).also {
        it.subscribe {
            onApiFilterUpdate()
        }
    }
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

    /* dynamic content only used to get _id */
    var overItem: Any? = null
    var menuOpenedState: Boolean? = null
    var navbarTabulator: NavbarTabulator<U>? = null
    var onDataLoadedTabulator: ((List<T>) -> Unit)? = null
    open val columnDefinitionList: List<ColumnDefinition<T>> = listOf()
    var masterViewItem: ViewItem<*, *>? = null
        set(value) {
            editable = value?.urlParams?.actionUpsert == true
            field = value
        }
    var offCanvasFilter: Offcanvas? = null
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
    var selectedIdList: List<Any?>? = null
    var tabulator: TabulatorListContainer<T, E, U, F>? = null
    var toolBarFilter: Boolean = false
    val toolBarListUpdateObservable = ObservableValue(0)

    /**
     * Builds a string URL for the CRUD action and item provided
     *
     * @param crudTask [CrudTask] element
     * @param item the item list selected
     */
    fun actionUrl(crudTask: CrudTask, item: T?): String? {
        val urlParams = if (crudTask == CrudTask.Create) {
            UrlParams(
                "action" to CrudTask.Create.name
            )
        } else {
            encodedId(item)?.let { id ->
                UrlParams(
                    "action" to crudTask.name,
                    "id" to id
                )
            }
        }
        masterViewItem?.let { viewItem ->
            urlParams?.addContext(viewItem.item, viewItem.encodedId())
        } ?: urlParams?.addContext(this@ViewList.urlParams?.apiList)
        return urlParams?.let {
            configViewItem?.let { it.url + urlParams.toString() }
        }
    }

    open fun onApiFilterUpdate() {
        updateBanner()
        AppScope.launch { dataUpdate() }
    }

    /**
     * On calling crud actions [[Create, Update]] on this list, checks if it has a masterViewItem
     * which is currently on Update action, if so, then performs an update call to back end before
     * calling the list crud action required
     */
    open fun checkIfmasterViewItemUpdate(url: String?) {
        if (masterViewItem?.urlParams?.crudTask == CrudTask.Update) {
            masterViewItem?.acceptUpsertAction { itemResponse ->
                if (itemResponse.isOk) {
                    url?.let { window.open(url = it, target = "_blank") }
                } else {
                    Toast.danger(itemResponse.msgError ?: "unknown error")
                }
            }
        } else {
            url?.let { window.open(url = url, target = "_blank") }
        }
    }

    open fun MutableList<TabulatorMenuItem>.contextRowMenu(item: T?) {}

    fun contextRowMenuGenerator(): Array<TabulatorMenuItem>? {
        val item: T? = overItem?.let {
            try {
                tabulator?.toKotlinObj(it)
            } catch (e: Exception) {
                Toast.danger(e.message ?: "")
                e.printStackTrace()
                null
            }
        }
        if (item != null) {
            val menu = mutableListOf<TabulatorMenuItem>()
            with(menu) {
                val labelId = configViewItem?.labelIdFunc?.invoke(item)
                menuItem(
                    label = " <font size=\"+1\">${configViewItem?.label}</font>: <b>$labelId</b>",
                    disabled = false,
                    header = true
                )
                menuItem(separator = true)
                val urlRead = actionUrl(CrudTask.Read, item)
                menuItem(
                    label = "Detail of",
                    icon = iconCrud(CrudTask.Read),
                    url = urlRead,
                    action = { _, _ ->
                        checkIfmasterViewItemUpdate(urlRead)
                    }
                )
                if (editable) {
                    val urlCreate = actionUrl(CrudTask.Create, item)
                    val urlUpdate = actionUrl(CrudTask.Update, item)
                    menuItem(separator = true)
                    menuItem(
                        label = "Create",
                        icon = iconCrud(CrudTask.Create),
                        url = urlCreate,
                        action = { _, _ ->
                            checkIfmasterViewItemUpdate(urlCreate)
                        }
                    )
                    menuItem(
                        label = "Update",
                        icon = iconCrud(CrudTask.Update),
                        url = urlUpdate,
                        action = { _, _ ->
                            checkIfmasterViewItemUpdate(urlUpdate)
                        }
                    )
                    menuItem(
                        label = "Delete",
                        icon = iconCrud(CrudTask.Delete),
                        url = actionUrl(CrudTask.Delete, item)
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
        if (!noPageBanner) {
            pageBanner()
        }
        toolBarFilter = offCanvasFilterView()?.let {
            offCanvasFilter = it
            true
        } ?: false
        pageListBody()
    }

    /**
     * Gets an [F] object for the [apiFilter] property from url parameters
     * Note: this needs that [apiFilter] be not null in order to get serializer
     */
    @OptIn(InternalSerializationApi::class)
    fun getApiFilterFromUrlParams() {
        val serializer = apiFilter.value?.let { it::class.serializer() } ?: configView.apiFilterKClass?.serializer()
        serializer?.let {
            urlParams?.pullUrlParam(serializer, "apiFilter")?.let {
                apiFilter.value = it
            }
        }
    }

    private fun encodedId(item: T?): String? {
        return item?.let { configView.encodedId(it._id) }
    }

    open fun Container.offCanvasFilterView(): Offcanvas? = null

    open fun onClickFilter() = offCanvasFilter?.show()

    open fun onRowSelected(item: T?) {}

    abstract fun Container.pageListBody()

    fun updateLinks(item: T?, size: Int) {
        val id = item?._id
        navbarTabulator?.itemId = id
//        navbarTabulator?.linkCreate?.url = actionUrl(CrudAction.Create, id)
        navbarTabulator?.linkRead?.url = actionUrl(CrudTask.Read, item)
        navbarTabulator?.linkUpdate?.url = actionUrl(CrudTask.Update, item)
        navbarTabulator?.linkUpdate?.target = "_blank"
        navbarTabulator?.linkDelete?.url = actionUrl(CrudTask.Delete, item)
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

    /**
     * Creates an [UrlParams] with the 'contextClass' and 'contextId' values from
     * the [item] parameter provided.
     */
    fun urlContext(item: T?): UrlParams {
        return UrlParams().addContext(item = item, encodedId(item))
    }
}
