package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.common.ICommonContainer
import com.fonrouge.fsLib.common.confirmDeleteView
import com.fonrouge.fsLib.commonServices.IApiCommonService
import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.config.ConfigViewItem.Companion.configViewItemMap
import com.fonrouge.fsLib.config.ConfigViewList
import com.fonrouge.fsLib.lib.iconCrud
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.CrudTask
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.apiData.IApiItem
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import com.fonrouge.fsLib.model.state.State
import com.fonrouge.fsLib.tabulator.NavbarTabulator
import com.fonrouge.fsLib.tabulator.TabulatorMenuItem
import com.fonrouge.fsLib.tabulator.TabulatorViewList
import com.fonrouge.fsLib.tabulator.menuItem
import io.kvision.core.Container
import io.kvision.remote.KVServiceManager
import io.kvision.state.ObservableValue
import io.kvision.tabulator.*
import io.kvision.tabulator.js.Tabulator
import io.kvision.toast.Toast
import kotlinx.browser.window

/**
 * Abstract class representing a list view with various properties and methods to manage and display collections of data items.
 *
 * @param CC Type of the common container.
 * @param T Type of the data item.
 * @param ID Type representing the identifier of the data item.
 * @param FILT Type of the API filter used.
 * @param MID Type representing the master item identifier.
 */
@Suppress("unused")
abstract class ViewList<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<MID>, MID : Any>(
    final override val configView: ConfigViewList<CC, T, ID, out ViewList<CC, T, ID, FILT, MID>, *, FILT, MID>,
    configViewItem: ConfigViewItem<CC, T, ID, *, IApiCommonService, FILT>? = null,
    periodicUpdateDataView: Boolean? = null,
    var editable: (() -> Boolean) = { true },
) : ViewDataContainer<CC, T, ID, FILT>(
    configViewContainer = configView,
) {
    /**
     * Represents a configuration holder for a view item in a ViewList.
     *
     * This variable allows lazy initialization and retrieval of a `ConfigViewItem` instance based on the
     * associated configuration view class. If the variable is not already initialized, it attempts to
     * resolve the corresponding view item configuration using naming conventions and a predefined
     * `configViewItemMap`. For example, if the class name of the view starts with "ViewList", it replaces
     * it with "ViewItem". Otherwise, it appends "ViewItem" with the name of the `itemKClass` in the
     * associated common container.
     *
     * This variable integrates with the shared state within the containing `ViewList` and is essential for
     * configuring CRUD-related operations, backend API interactions, and other functionalities tied to
     * the view item.
     */
    private var _configViewItem: ConfigViewItem<CC, T, ID, *, IApiCommonService, FILT>? = configViewItem
    fun configViewItem(): ConfigViewItem<CC, T, ID, *, IApiCommonService, FILT>? {
        if (_configViewItem != null) return _configViewItem
        val viewClassName = configView.viewKClass.simpleName!!
        val name = if (viewClassName.startsWith("ViewList")) {
            viewClassName.replaceFirst("ViewList", "ViewItem")
        } else {
            "ViewItem${configView.commonContainer.itemKClass.js.name}"
        }
        _configViewItem = configViewItemMap[name]?.unsafeCast<ConfigViewItem<CC, T, ID, *, IApiCommonService, FILT>>()
        return _configViewItem
    }

    open val columnDefaults: ColumnDefinition<T>? = ColumnDefinition(title = "", headerSort = false)

    val errorStateObs = ObservableValue(false)
    var errorMessage: String? = null

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

    open val rowSelectedColumn: ColumnDefinition<T> = ColumnDefinition(
        title = "<i class=\"fa-regular fa-square\"></i>",
        field = "rowSelected",
        hozAlign = Align.CENTER,
        formatter = Formatter.ROWSELECTION
    )

    private fun buildColumnDefinitionDeleteItem(
        cellClick: ((e: Any?, cell: Tabulator.CellComponent) -> Unit)
    ): ColumnDefinition<T> = ColumnDefinition<T>(
        title = "",
        field = "deleteItem",
        hozAlign = Align.CENTER,
        formatterFunction = { _, _, _ ->
            "<i class=\"fa-solid fa-trash\"></i>"
        },
        cellClick = cellClick
    )

    /**
     * Defines a column for deleting items in a tabular view. Upon interaction with this column,
     * a confirmation dialog is displayed to confirm the deletion of the selected item. If confirmed,
     * the related delete operation is executed.
     *
     * The function utilizes a configuration provided by the `configViewItem` property to determine
     * the behavior of the confirmation dialog and the deletion process, allowing for specific
     * implementations of delete actions via the `serviceManager` and `apiItemFun`.
     *
     * @param AIS The type of the API service, extending `IApiCommonService`.
     * @return A `ColumnDefinition` for the delete column, with preconfigured behavior for deletion.
     */
    fun <AIS : IApiCommonService> columnDefinitionDeleteItem(): ColumnDefinition<T> =
        buildColumnDefinitionDeleteItem { _, cell ->
            cell.item?.let { item ->
                configViewItem()?.let { configViewItem ->
                    configViewItem.commonContainer.confirmDeleteView(
                        serviceManager = configViewItem.serviceManager,
                        apiItemFun = configViewItem.apiItemFun,
                        item = item
                    )
                } ?: console.error("No configViewItem found")
            }
        }

    /**
     * Defines a column for deleting items in a tabular view. When a user interacts with this column,
     * a confirmation dialog is displayed to confirm the deletion of the selected item. Upon confirmation,
     * the delete operation is executed, and the view updates accordingly.
     *
     * @param AIS The type of the API service, extending `IApiCommonService`.
     * @param serviceManager Manages API services, providing access to perform API operations.
     * @param apiItemFun A suspendable function invoked on the API service to handle the deletion process.
     * This function takes an API item and performs the corresponding delete operation.
     * @return A `ColumnDefinition` configured for the delete column, enabling item deletion functionality within the tabular view.
     */
    fun <AIS : IApiCommonService> columnDefinitionDeleteItem(
        serviceManager: KVServiceManager<AIS>,
        apiItemFun: (suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>),
    ): ColumnDefinition<T> = buildColumnDefinitionDeleteItem { _, cell ->
        cell.item?.let { item ->
            configView.commonContainer.confirmDeleteView(
                serviceManager = serviceManager,
                apiItemFun = apiItemFun,
                item = item,
                onSuccess = { dataUpdate() }
            )
        }
    }

    open fun columnDefinitionList(): List<ColumnDefinition<T>> = listOf()

    //    val columnList: List<ColumnDefinition<T>> get() = listOfNotNull(rowSelectedColumn) + columnDefinitionList()
    var masterViewItem: ViewItem<out ICommonContainer<out BaseDoc<MID>, MID, *>, out BaseDoc<MID>, MID, *, *>? = null
        set(value) {
            apiFilter.masterItemId = value?.item?._id
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
    @Suppress("MemberVisibilityCanBePrivate")
    var markReloadColumnDefinitions = false

    /**
     * Contains id's from selected row(s)
     */
    var selectedIdList: List<Any?>? = null

    /**
     * the tabulator list
     */
    var tabulator: TabulatorViewList<T, ID, FILT, MID>? = null

    /**
     * On calling crud actions [[Create, Update]] on this list, checks if it has a masterViewItem
     * which is currently on Update action, if so, then performs an update call to back end before
     * calling the list crud action required
     */
    open fun goActionUrl(
        crudTask: CrudTask,
        item: T? = selectedItemObs.value,
        configViewItem: ConfigViewItem<CC, T, ID, *, IApiCommonService, FILT>? = this.configViewItem(),
    ) {
        configViewItem ?: return
        val apiItem = when (crudTask) {
            CrudTask.Create -> ApiItem.Upsert.Create.Query(apiFilter = apiFilter)
            CrudTask.Read -> item?._id?.let { ApiItem.Read<T, ID, FILT>(id = item._id, apiFilter = apiFilter) }
            CrudTask.Update -> item?._id?.let {
                ApiItem.Upsert.Update.Query<T, ID, FILT>(
                    id = item._id,
                    apiFilter = apiFilter
                )
            }

            CrudTask.Delete -> item?._id?.let {
                ApiItem.Delete.Query<T, ID, FILT>(
                    id = item._id,
                    apiFilter = apiFilter
                )
            }
        } ?: return
        val url = configViewItem.viewItemUrl(
            apiItem = apiItem
        )
        val callBlock = {
            if (crudTask == CrudTask.Delete) {
                item?.let {
                    configViewItem.commonContainer.confirmDeleteView(
                        serviceManager = configViewItem.serviceManager,
                        apiItemFun = configViewItem.apiItemFun,
                        item = item,
                        apiFilter = apiFilter
                    ) {
                        dataUpdate()
                    }
                }
            } else {
                url?.let { window.open(url = url, target = "_blank") }
            }
        }
        if (masterViewItem?.urlParams?.crudTask == CrudTask.Update) {
            masterViewItem?.acceptUpsertAction { itemResponse ->
                if (itemResponse.state != State.Error) {
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
        val configViewItem = configViewItem()
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
    final override fun dataUpdate() {
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
        val columnList = columnDefinitionList()
        tabulator?.let { tabulator ->
            tabulator.jsTabulator?.setColumns(columnList.map {
                it.toJs(tabulator, tabulator::translate, configView.commonContainer.itemKClass)
            }.toTypedArray())
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
     * Retrieves the item associated with the current cell in the Tabulator.
     *
     * This property gets the data of the current cell and converts it to
     * a Kotlin object. If the `tabulator` is null, an exception is thrown.
     *
     * @throws Throwable If the `tabulator` is null, the method will throw an exception.
     * @return The data item represented by the current cell, converted to a Kotlin object.
     *
     * TODO: try/catch needed because tabulator column 'editable' property
     * can send wrong T structure and it throws error on deserialization
     * combined with using the bottomCalcFun ColumnDefinition property
     */
    val Tabulator.CellComponent.item: T?
        get() = try {
            tabulator?.toKotlinObj(this.getData())
        } catch (_: Exception) {
//            console.error(e)
            null
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
     * the main display for the viewList tabulator area
     */
    abstract fun Container.pageListBody()
}
