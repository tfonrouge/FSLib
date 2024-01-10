package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.config.ConfigViewItem.Companion.configViewItemMap
import com.fonrouge.fsLib.config.ConfigViewList
import com.fonrouge.fsLib.config.ICommonItem
import com.fonrouge.fsLib.config.ICommonList
import com.fonrouge.fsLib.layout.NavbarTabulator
import com.fonrouge.fsLib.layout.TabulatorListContainer
import com.fonrouge.fsLib.layout.TabulatorMenuItem
import com.fonrouge.fsLib.layout.menuItem
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.lib.iconCrud
import com.fonrouge.fsLib.model.CrudTask
import com.fonrouge.fsLib.model.IDataList
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import io.kvision.core.Container
import io.kvision.state.ObservableValue
import io.kvision.tabulator.ColumnDefinition
import io.kvision.tabulator.RowRangeLookup
import io.kvision.tabulator.js.Tabulator
import io.kvision.tabulator.toJs
import io.kvision.toast.Toast
import kotlinx.browser.window

@Suppress("unused")
abstract class ViewList<CV : ICommonList<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, E : IDataList, FILT : IApiFilter>(
    urlParams: UrlParams? = null,
    final override val configView: ConfigViewList<CV, T, ID, out ViewList<CV, T, ID, E, FILT>, E, FILT>,
    configViewItem: ConfigViewItem<ICommonItem<T, ID, FILT>, T, ID, *, *, FILT>? = null,
    periodicUpdateDataView: Boolean? = null,
    editable: Boolean = true,
    icon: String? = null,
) : ViewDataContainer<CV, FILT>(
    urlParams = urlParams,
    configViewContainer = configView,
    editable = editable,
    icon = icon,
) {
    var allowInstallUpdate: Boolean = true

    /**
     * contains the configViewItem descriptor, it can be assigned programmatically or calculated from configViewItem map
     * matching by name
     */
    var configViewItem: ConfigViewItem<ICommonItem<T, ID, FILT>, T, ID, *, *, FILT>? = configViewItem
        get() {
            if (field != null) return field
            val viewClassName = configView.viewFunc.simpleName!!
            val name = if (viewClassName.contains("ViewList")) {
                viewClassName.replace("ViewList", "ViewItem")
            } else {
                "ViewItem${configView.itemKClass.js.name}"
            }
            return configViewItemMap[name]?.unsafeCast<ConfigViewItem<ICommonItem<T, ID, FILT>, T, ID, *, *, FILT>>()
        }

    open val columnDefaults: ColumnDefinition<T>? = null

    /**
     * contains an object of [T] type for the selected row in the [tabulator]
     */
    var selectedItem: T? = null
    var jsTabulatorBuilt: Boolean = false
    var menuOpenedState: Boolean? = null
    var navbarTabulator: NavbarTabulator<ID>? = null
    var onDataLoadedTabulator: ((List<T>) -> Unit)? = null

    /* dynamic content only used to get _id */
    var overItem: Any? = null
    open fun columnDefinitionList(): List<ColumnDefinition<T>> = listOf()
    var masterViewItem: ViewItem<*, *, *, *>? = null
        set(value) {
            apiFilter.value?.masterItemIdSerialized = value?.encodeId()
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
    private var reloadColumnDefinitions = false
    fun reloadColumnDefinitions() {
        reloadColumnDefinitions = true
    }

    var selectedIdList: List<Any?>? = null

    /**
     * the tabulator list
     */
    var tabulator: TabulatorListContainer<T, ID, E, FILT>? = null

    /**
     * observable that triggers an update on the list's toolbar
     */
    val toolBarListUpdateObservable = ObservableValue(0)

    /**
     * On calling crud actions [[Create, Update]] on this list, checks if it has a masterViewItem
     * which is currently on Update action, if so, then performs an update call to back end before
     * calling the list crud action required
     */
    open fun goActionUrl(
        crudTask: CrudTask,
        item: T? = selectedItem,
        configViewItem: ConfigViewItem<ICommonItem<T, ID, FILT>, *, ID, *, *, FILT>? = this.configViewItem,
    ) {
        val url = configViewItem?.let {
            urlFromApiItem(
                configViewItem = configViewItem,
                apiItem = ApiItem(
                    id = item?._id,
                    item = item,
                    crudTask = crudTask,
                    apiFilter = apiFilter.value
                )
            )
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
                val labelId = configViewItem?.commonView?.labelIdFunc?.invoke(item)
                menuItem(
                    label = " <font size=\"+1\">${configViewItem?.commonView?.label}</font>: <b>$labelId</b>",
                    disabled = false,
                    header = true
                )
                menuItem(separator = true)
                menuItem(
                    label = "Detail of",
                    icon = iconCrud(CrudTask.Read),
                    action = { _, _ ->
                        goActionUrl(CrudTask.Read, item)
                    }
                )
                if (editable) {
                    menuItem(separator = true)
                    menuItem(
                        label = "Create",
                        icon = iconCrud(CrudTask.Create),
                        action = { _, _ ->
                            goActionUrl(CrudTask.Create, item)
                        }
                    )
                    menuItem(
                        label = "Update",
                        icon = iconCrud(CrudTask.Update),
                        action = { _, _ ->
                            goActionUrl(CrudTask.Update, item)
                        }
                    )
                    menuItem(
                        label = "Delete",
                        icon = iconCrud(CrudTask.Delete),
                        action = { _, _ ->
                            goActionUrl(CrudTask.Delete, item)
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
    final override suspend fun dataUpdate() {
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
     * the main display for the viewList, displays the [pageBanner] and the [buildOffCanvasFilterView] if any defined
     */
    override fun Container.displayPage() {
        if (!noPageBanner) {
            pageBanner()
        }
        pageListBody()
    }

    /**
     * Gets an [T] item from a [Tabulator.CellComponent]
     *
     * @param cell
     * @return [T] item
     */
    fun getItem(cell: Tabulator.CellComponent): T? {
        return tabulator?.toKotlinObj(cell.getData())
    }

    /**
     * export to file download
     */
    open fun outToFile() {
        tabulator?.downloadCSV(
            fileName = "${label}.csv",
            dataSet = RowRangeLookup.ALL,
            includeBOM = true
        )
    }

    /**
     * print viewList
     */
    open fun outPrint() {
        tabulator?.print(rowRangeLookup = RowRangeLookup.ALL, isStyled = true)
    }

    /**
     * open function that fires when a row is selected in the tabulator
     */
    var onRowSelected: ((T?) -> Unit)? = null

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
}
