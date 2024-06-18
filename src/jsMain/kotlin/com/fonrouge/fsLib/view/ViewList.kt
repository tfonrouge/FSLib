package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.config.ConfigViewItem.Companion.configViewItemMap
import com.fonrouge.fsLib.config.ConfigViewList
import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.layout.NavbarTabulator
import com.fonrouge.fsLib.layout.TabulatorListContainer
import com.fonrouge.fsLib.layout.TabulatorMenuItem
import com.fonrouge.fsLib.layout.menuItem
import com.fonrouge.fsLib.lib.iconCrud
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.CrudTask
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
import kotlinx.coroutines.launch

@Suppress("unused")
abstract class ViewList<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, E : Any, FILT : IApiFilter>(
    final override val configView: ConfigViewList<CC, T, ID, out ViewList<CC, T, ID, E, FILT>, E, FILT>,
    configViewItem: ConfigViewItem<ICommonContainer<T, ID, FILT>, T, ID, *, *, FILT>? = null,
    periodicUpdateDataView: Boolean? = null,
    editable: (() -> Boolean) = { true },
    icon: String? = null,
) : ViewDataContainer<CC, T, ID, FILT>(
    configViewContainer = configView,
    editable = editable,
    icon = icon,
) {
    var allowInstallUpdate: Boolean = true

    /**
     * contains the configViewItem descriptor, it can be assigned programmatically or calculated from configViewItem map
     * matching by name
     */
    var configViewItem: ConfigViewItem<ICommonContainer<T, ID, FILT>, T, ID, *, *, FILT>? = configViewItem
        get() {
            if (field != null) return field
            val viewClassName = configView.viewKClass.simpleName!!
            val name = if (viewClassName.contains("ViewList")) {
                viewClassName.replace("ViewList", "ViewItem")
            } else {
                "ViewItem${configView.commonContainer.itemKClass.js.name}"
            }
            return configViewItemMap[name]?.unsafeCast<ConfigViewItem<ICommonContainer<T, ID, FILT>, T, ID, *, *, FILT>>()
        }

    open val columnDefaults: ColumnDefinition<T>? = ColumnDefinition(title = "", headerSort = false)

    /**
     * contains an object of [T] type for the selected row in the [tabulator]
     */
    var selectedItemObs: ObservableValue<T?> = ObservableValue(null)
    var jsTabulatorBuilt: Boolean = false
    var menuOpenedState: Boolean? = null
    var navbarTabulator: NavbarTabulator? = null
    var onDataLoadedTabulator: ((List<T>) -> Unit)? = null

    /* dynamic content only used to get _id */
    var overItem: Any? = null
    open fun columnDefinitionList(): List<ColumnDefinition<T>> = listOf()
    var masterViewItem: ViewItem<*, *, *, *>? = null
        set(value) {
            apiFilter.masterItemIdSerialized = value?.encodeId()
            editable = { value?.urlParams?.actionUpsert == true }
            field = value
        }

    /**
     * Set to true if periodic update of table data is allowed
     */
    final override var periodicUpdateDataView: Boolean? = periodicUpdateDataView
        get() = field ?: KVWebManager.periodicUpdateDataViewList

    /**
     * Set to true to reload column definitions on next [dataUpdate]
     */
    var markReloadColumnDefinitions = false

    /**
     * Contains id's from selected row(s)
     */
    var selectedIdList: List<Any?>? = null

    /**
     * the tabulator list
     */
    var tabulator: TabulatorListContainer<T, ID, E, FILT>? = null

    /**
     * On calling crud actions [[Create, Update]] on this list, checks if it has a masterViewItem
     * which is currently on Update action, if so, then performs an update call to back end before
     * calling the list crud action required
     */
    open fun goActionUrl(
        crudTask: CrudTask,
        item: T? = selectedItemObs.value,
        configViewItem: ConfigViewItem<ICommonContainer<T, ID, FILT>, T, ID, *, *, FILT>? = this.configViewItem,
    ) {
        configViewItem ?: return
        val apiItem = ApiItem.Query.build(
            commonContainer = configViewItem.commonContainer,
            id = item?._id,
            crudTask = crudTask,
            apiFilter = apiFilter
        ) ?: return
        val url = configViewItem.viewItemUrl(
            apiItem = apiItem
        )
        val callBlock = {
            if (crudTask == CrudTask.Delete) {
                item?.let {
                    confirmDeleteView(item, configViewItem, apiFilter = apiFilter) {
                        AppScope.launch { dataUpdate() }
                    }
                }
            } else {
                url?.let { window.open(url = url, target = "_blank") }
            }
        }
        if (masterViewItem?.urlParams?.crudTask == CrudTask.Update) {
            masterViewItem?.acceptUpsertAction { itemResponse ->
                if (itemResponse.isOk) {
                    callBlock()
                } else {
                    Toast.danger(itemResponse.msgError ?: "unknown error")
                }
            }
        } else {
            callBlock()
        }
    }

    open fun MutableList<TabulatorMenuItem>.contextRowMenu(item: T?) {}

    fun contextRowMenuGenerator(): Array<TabulatorMenuItem> {
        val item: T? = overItem?.let {
            try {
                tabulator?.toKotlinObj(it)
            } catch (e: Exception) {
                Toast.danger(e.message ?: "")
                e.printStackTrace()
                null
            }
        }
        val configViewItem = configViewItem
        val menu = mutableListOf<TabulatorMenuItem>()
        with(menu) {
            val labelId = configViewItem?.commonContainer?.labelIdFunc?.invoke(item) ?: ""
            menuItem(
                label = " <font size=\"+1\">${configViewItem?.label ?: ""}</font>: <b>$labelId</b>",
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
            if (editable()) {
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

    /**
     * forces an update for the tabulator data
     */
    final override suspend fun dataUpdate() {
        if (jsTabulatorBuilt) {
            if (markReloadColumnDefinitions) {
                markReloadColumnDefinitions = false
                loadColumnDefinitions()
            }
            if (menuOpenedState != true) {
                selectedIdList = tabulator?.getSelectedData()?.map { it._id }
                tabulator?.apiCall()
            }
        }
    }

    /**
     * load the column definitions from [columnDefinitionList] to the [tabulator]
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun loadColumnDefinitions() {
        tabulator?.let { tabulator ->
            tabulator.jsTabulator?.setColumns(
                columnDefinitionList().map {
                    it.toJs(tabulator, tabulator::translate, configView.commonContainer.itemKClass)
                }.toTypedArray()
            )
            //columnDefinitionList()
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

    override val label: String get() = configView.commonContainer.labelList

    open fun NavbarTabulator.navBarOptions() {}

    /**
     * allows to process javascript array arrived from backend
     */
    open fun onReceivingData(data: dynamic) {}

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
}
