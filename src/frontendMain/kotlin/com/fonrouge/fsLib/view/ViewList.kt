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
abstract class ViewList<T : BaseDoc<ID>, E : IDataList, ID : Any, FILT : Any, STATE : Any>(
    override val configView: ConfigViewList<T, out ViewList<T, E, ID, FILT, STATE>, E, ID, FILT, STATE>,
    configViewItem: ConfigViewItem<T, *, *, ID, STATE>? = null,
    periodicUpdateDataView: Boolean? = null,
    editable: Boolean = true,
    icon: String? = null,
    /**
     * If apiFilter kclass is not defined in [configView] this value needs to be initialized
     * on view construct in order to automatically get [apiFilter] parameter from url params
     */
    apiFilter: FILT? = null,
) : ViewDataContainer(
    configView = configView,
    editable = editable,
    icon = icon,
) {
    /**
     * observable that contains an [FILT] object. It can be assigned from an apiFilter= url parameter
     * or programmatically, and it's delivered to the backend
     */
    val apiFilter: ObservableValue<FILT?> = ObservableValue(apiFilter).also {
        it.subscribe {
            onApiFilterUpdate()
        }
    }
    var configViewItem: ConfigViewItem<T, *, *, ID, STATE>? = configViewItem
        get() {
            if (field != null) return field
            val viewClassName = configView.viewFunc.simpleName!!
            val name = if (viewClassName.contains("ViewList")) {
                viewClassName.replace("ViewList", "ViewItem")
            } else {
                "ViewItem${configView.itemKClass.js.name}"
            }
            return configViewItemMap[name]?.unsafeCast<ConfigViewItem<T, *, *, ID, STATE>>()
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
    open val columnDefinitionList: List<ColumnDefinition<T>> = listOf()
    var masterViewItem: ViewItem<*, *, *>? = null
        set(value) {
            editable = value?.urlParams?.actionUpsert == true
            field = value
        }

    /**
     * assignable var that contains a defined [Offcanvas] filter area, if any
     */
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
     * open function that allows to override the default action when the [apiFilter] observable changes.
     * The default action will do an [updateBanner] and then an [dataUpdate]
     */
    open fun onApiFilterUpdate() {
        updateBanner()
        AppScope.launch { dataUpdate() }
    }

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
        }?.let {
            val urlParams = UrlParams(*it.toTypedArray())
            configViewItem?.let { configViewItem1 ->
                setApiState(crudTask, item)?.let { s ->
                    urlParams.pushParam(
                        configViewItem1.pairParam(
                            "apiState",
                            configViewItem1.apiStateKClass.serializer(),
                            s
                        )
                    )
                }
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

    /**
     * Gets an [FILT] object for the [apiFilter] property from url parameters
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

    /**
     * Builds the url for the viewItem call.
     * Can be overridden in order to add custom params to url
     */
    open suspend fun setApiState(crudTask: CrudTask, item: T?): STATE? = null

    /**
     * open function that builds a filter form
     */
    open fun Container.offCanvasFilterView(): Offcanvas? = null

    /**
     * open function that fires when filter button on toolbar is clicked
     */
    open fun onClickFilter() = offCanvasFilter?.show()

    /**
     * open function that fires when a row is selected in the tabulator
     */
    open fun onRowSelected(item: T?) {}

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

    /**
     * Creates an [UrlParams] with the 'contextClass' and 'contextId' values from
     * the [item] parameter provided.
     */
    fun urlContext(item: T?): UrlParams {
        return UrlParams().addContext(item = item, encodedId(item))
    }
}
