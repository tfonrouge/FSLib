package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.common.ICommonContainer
import com.fonrouge.fsLib.common.confirmDeleteView
import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.config.ConfigViewList
import com.fonrouge.fsLib.lib.iconCrud
import com.fonrouge.fsLib.lib.toast
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.CrudTask
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.apiData.setMasterItemId
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import com.fonrouge.fsLib.model.state.SimpleState
import com.fonrouge.fsLib.model.state.State
import com.fonrouge.fsLib.tabulator.NavbarTabulator
import com.fonrouge.fsLib.tabulator.TabulatorMenuItem
import com.fonrouge.fsLib.tabulator.TabulatorViewList
import com.fonrouge.fsLib.tabulator.menuItem
import com.fonrouge.fsLib.view.KVWebManager.configViewItemMap
import io.kvision.core.Container
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
abstract class ViewList<out CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<MID>, MID : Any>(
    final override val configView: ConfigViewList<CC, T, ID, out ViewList<CC, T, ID, FILT, MID>, FILT, MID, *>,
    configViewItem: ConfigViewItem<CC, T, ID, *, FILT, *>? = null,
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
    private var _configViewItem: ConfigViewItem<CC, T, ID, *, FILT, *>? = configViewItem
    fun configViewItem(): ConfigViewItem<CC, T, ID, *, FILT, *>? {
        if (_configViewItem != null) return _configViewItem
        val viewClassName = configView.viewKClass.simpleName!!
        val name = if (viewClassName.startsWith("ViewList")) {
            viewClassName.replaceFirst("ViewList", "ViewItem")
        } else {
            "ViewItem${configView.commonContainer.itemKClass.js.name}"
        }
        _configViewItem = configViewItemMap[name]?.unsafeCast<ConfigViewItem<CC, T, ID, *, FILT, *>>()
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
        field = "__rowSelected",
        hozAlign = Align.CENTER,
        formatter = Formatter.ROWSELECTION
    )

    /**
     * Determines whether the default context menu for a row should be displayed.
     *
     * This property provides a lambda function returning a Boolean value
     * that specifies whether the default context row menu is enabled or not.
     * By default, it always returns `true`, indicating that the menu is shown.
     * It can be customized to modify the condition dynamically based on specific logic.
     */
    var showDefaultContextRowMenu: () -> Boolean = { true }

    private fun buildColumnDefinitionDeleteItem(
        visible: Boolean? = null,
        cellClick: ((e: Any?, cell: Tabulator.CellComponent) -> Unit),
    ): ColumnDefinition<T> = ColumnDefinition<T>(
        title = "",
        field = "__deleteItem",
        vertAlign = VAlign.MIDDLE,
        hozAlign = Align.CENTER,
        formatterFunction = { _, _, _ ->
            "<i class=\"fa-solid fa-trash\"></i>"
        },
        visible = visible,
        cellClick = cellClick
    )

    /**
     * Defines a column for the deletion of items in a tabular view.
     *
     * This method configures a column that provides a delete functionality for items.
     * It integrates a confirmation dialog for user confirmation before performing a delete action.
     * If a configuration view item is not available, an error is logged and displayed.
     *
     * @param visible An optional parameter to control the visibility of the delete column.
     *                If `null`, the visibility is determined by the default configuration.
     * @return A `ColumnDefinition<T>` instance that represents the delete column.
     */
    fun columnDefinitionDeleteItem(visible: Boolean? = null): ColumnDefinition<T> =
        buildColumnDefinitionDeleteItem(visible = visible) { _, cell ->
            cell.item?.let { item ->
                configViewItem()?.let { configViewItem ->
                    configViewItem.commonContainer.confirmDeleteView(
                        apiItemFun = configViewItem.apiItemFun,
                        item = item
                    )
                } ?: run {
                    "No configViewItem found".also {
                        console.error(it)
                        SimpleState(
                            state = State.Error,
                            msgError = it
                        ).toast()
                    }
                }
            }
        }

    /**
     * Creates and configures a column definition that provides a delete functionality for items in a tabular view.
     *
     * This column allows users to delete items from the tabular view. It integrates a confirmation dialog
     * to ensure user approval before executing the delete operation. The delete operation uses the specified
     * item state function and refreshes the data view upon success.
     *
     * @param visible An optional parameter to determine the visibility of the delete column. Defaults to `null`,
     *                allowing the default visibility settings to apply.
     * @param apiItemFun A function that operates on the item's state and manages the delete operation.
     * @return A `ColumnDefinition<T>` instance representing the delete column with appropriate functionality.
     */
    fun columnDefinitionDeleteItem(
        visible: Boolean? = null,
        apiItemFun: Function<ItemState<T>>,
    ): ColumnDefinition<T> = buildColumnDefinitionDeleteItem(visible = visible) { _, cell ->
        cell.item?.let { item ->
            configView.commonContainer.confirmDeleteView(
                apiItemFun = apiItemFun,
                item = item,
                onSuccess = { dataUpdate() }
            )
        }
    }

    open fun columnDefinitionList(): List<ColumnDefinition<T>> = emptyList()

    var masterViewItem: ViewItem<ICommonContainer<out BaseDoc<MID>, MID, *>, out BaseDoc<MID>, MID, *>? = null
        set(value) {
            editable = { value?.actionUpsert == true }
            crudTask = value?.crudTask
            apiFilter = apiFilter.setMasterItemId(value?.item?._id)
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
        configViewItem: ConfigViewItem<@UnsafeVariance CC, T, ID, *, FILT, *>? = configViewItem(),
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
        if (masterViewItem?.crudTask == CrudTask.Update) {
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
            if (showDefaultContextRowMenu()) {
                menuItem(
                    label = "Detail of",
                    icon = iconCrud(CrudTask.Read),
                    action = { _, _ ->
                        goActionUrl(CrudTask.Read, item)
                    }
                )
            }
            if (editable() && showDefaultContextRowMenu()) {
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
