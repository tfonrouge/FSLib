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
import io.kvision.tabulator.toJs
import io.kvision.toast.Toast
import js.uri.encodeURIComponent
import kotlinx.browser.window
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import web.buffer.btoa

@Suppress("unused")
abstract class ViewList<T : BaseDoc<ID>, E : IDataList, ID : Any, FILT : Any>(
    final override val configView: ConfigViewList<T, out ViewList<T, E, ID, FILT>, E, ID, FILT>,
    configViewItem: ConfigViewItem<T, *, *, ID, FILT>? = null,
    periodicUpdateDataView: Boolean? = null,
    editable: Boolean = true,
    icon: String? = null,
    /**
     * If apiFilter kclass is not defined in [configView] this value needs to be initialized
     * on view construct in order to automatically get [apiFilter] parameter from url params
     *
     * Note: [ConfigViewList.apiFilterKClass] class must haven't constructor parameters
     */
    apiFilter: FILT? = null,
) : ViewDataContainer<FILT>(
    configViewContainer = configView,
    apiFilter = apiFilter,
    editable = editable,
    icon = icon,
) {
    /**
     * contains the configViewItem descriptor, it can be assigned programmatically or calculated from configViewItem map
     * matching by name
     */
    var configViewItem: ConfigViewItem<T, *, *, ID, FILT>? = configViewItem
        get() {
            if (field != null) return field
            val viewClassName = configView.viewFunc.simpleName!!
            val name = if (viewClassName.contains("ViewList")) {
                viewClassName.replace("ViewList", "ViewItem")
            } else {
                "ViewItem${configView.itemKClass.js.name}"
            }
            return configViewItemMap[name]?.unsafeCast<ConfigViewItem<T, *, *, ID, FILT>>()
        }

    /**
     * contains an object of [T] type for the selected row in the [tabulator]
     */
    var selectedItem: T? = null
    var jsTabulatorBuilt: Boolean = false

    /* dynamic content only used to get _id */
    var overItem: Any? = null
    var menuOpenedState: Boolean? = null
    var navbarTabulator: NavbarTabulator<ID>? = null
    var onDataLoadedTabulator: ((List<T>) -> Unit)? = null
    open fun columnDefinitionList(): List<ColumnDefinition<T>> = listOf()
    var masterViewItem: ViewItem<*, *, *>? = null
        set(value) {
            editable = value?.urlParams?.actionUpsert == true
            field = value
        }

    /**
     * assignable var that contains a defined [Offcanvas] filter area, if any
     */
    @Suppress("MemberVisibilityCanBePrivate")
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
    private var reloadColumnDefinitions = false
    fun reloadColumnDefinitions() {
        reloadColumnDefinitions = true
    }

    var selectedIdList: List<Any?>? = null

    /**
     * the tabulator list
     */
    var tabulator: TabulatorListContainer<T, E, ID, FILT>? = null

    /**
     * assignable var that indicates if the filter button in the tabulator's toolbar will be displayed
     */
    var toolBarFilter: Boolean = false

    /**
     * observable that triggers an update on the list's toolbar
     */
    val toolBarListUpdateObservable = ObservableValue(0)

    /**
     * On calling crud actions [[Create, Update]] on this list, checks if it has a masterViewItem
     * which is currently on Update action, if so, then performs an update call to back end before
     * calling the list crud action required
     */
    @OptIn(InternalSerializationApi::class)
    open suspend fun goActionUrl(
        crudTask: CrudTask,
        item: T? = selectedItem
    ) {
        val url: String? = when (crudTask) {
            CrudTask.Create -> listOf("action" to CrudTask.Create.name)
            else -> {
                encodedId(item)?.let { id ->
                    listOf("action" to crudTask.name, "id" to id)
                }
            }
        }?.let { params ->
            val urlParams = UrlParams(*params.toTypedArray())
            onSetContext()?.let {
                urlParams.addContext(it)
            }
            configViewItem?.let { configViewItem1 ->
                if (configView.apiFilterKClass != Unit::class) {
                    urlParams.pushParam(
                        "apiFilter" to encodeURIComponent(
                            btoa(
                                Json.encodeToString(
                                    configView.apiFilterKClass.serializer(),
                                    apiFilter.value
                                )
                            )
                        )
                    )
                }
                apiFilter.value
                masterViewItem?.let { viewItem ->
                    urlParams.addContext(viewItem.item, viewItem.encodedId())
                } ?: urlParams.addContext(this@ViewList.urlParams?.apiList)
                configViewItem1.url + urlParams.toString()
            }
        }
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
                menuItem(
                    label = "Detail of",
                    icon = iconCrud(CrudTask.Read),
                    action = { _, _ ->
                        AppScope.launch {
                            goActionUrl(CrudTask.Read, item)
                        }
                    }
                )
                if (editable) {
                    menuItem(separator = true)
                    menuItem(
                        label = "Create",
                        icon = iconCrud(CrudTask.Create),
                        action = { _, _ ->
                            AppScope.launch {
                                goActionUrl(CrudTask.Create, item)
                            }
                        }
                    )
                    menuItem(
                        label = "Update",
                        icon = iconCrud(CrudTask.Update),
                        action = { _, _ ->
                            AppScope.launch {
                                goActionUrl(CrudTask.Update, item)
                            }
                        }
                    )
                    menuItem(
                        label = "Delete",
                        icon = iconCrud(CrudTask.Delete),
                        action = { _, _ ->
                            AppScope.launch {
                                goActionUrl(CrudTask.Delete, item)
                            }
                        }
                    )
                }
                contextRowMenu(item)
            }
            return menu.toTypedArray()
        }
        return null
    }

    /**
     * forces an update for the tabulator data
     */
    override suspend fun dataUpdate() {
        if (jsTabulatorBuilt) {
            if (reloadColumnDefinitions) {
                reloadColumnDefinitions = false
                tabulator?.let { tabulator ->
                    tabulator.jsTabulator?.setColumns(
                        columnDefinitionList().map {
                            it.toJs(tabulator, tabulator::translate, configView.itemKClass)
                        }.toTypedArray()
                    )
                    columnDefinitionList()
                }
            }
            if (menuOpenedState != true) {
                selectedIdList = tabulator?.getSelectedData()?.map { it._id }
                tabulator?.apiCall()
            }
        }
    }

    /**
     * the main display for the viewList, displays the [pageBanner] and the [offCanvasFilterView] if any defined
     */
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

    private fun encodedId(item: T?): String? {
        return item?.let { configView.encodedId(it._id) }
    }

    /**
     * open function that builds a filter form
     */
    open fun Container.offCanvasFilterView(): Offcanvas? = null

    /**
     * open function that fires when toolbar's filter button is clicked. If [apiFilter] contains a null value then
     * a new [FILT] object is created (with no constructor parameters) and assign it to the apiFilter value.
     */
    open fun onClickFilter() {
        offCanvasFilter?.show()
    }

    /**
     * open function that fires when a row is selected in the tabulator
     */
    open fun onRowSelected(item: T?) {}

    /**
     * allows to set a custom context to be included in url params
     */
    @Deprecated(message = "contextClass/contextId url params", replaceWith = ReplaceWith("ApiFilter [FILT]"))
    open fun onSetContext(): Pair<String?, String>? = null

    /**
     * the main display for the viewList tabulator area
     */
    abstract fun Container.pageListBody()

    fun updateLinks(item: T?, size: Int) {
        val id = item?._id
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

    fun <F : Any> urlApiFilter(
        configViewList: ConfigViewList<*, *, *, *, F>,
        apiFilter: F,
    ): String {
        val params = mutableListOf<Pair<String, String>>()
        urlParams?.contextClass?.let { params.add("contextClass" to it) }
        urlParams?.contextId?.let { params.add("contextId" to it) }
        params.add(configViewList.apiFilterParam(apiFilter))
        return configViewList.urlWithParams(*params.toTypedArray())
    }

    /**
     * Creates an [UrlParams] with the 'contextClass' and 'contextId' values from
     * the [item] parameter provided.
     */
    fun urlContext(item: T?): UrlParams {
        return UrlParams().addContext(item = item, encodedId(item))
    }
}
