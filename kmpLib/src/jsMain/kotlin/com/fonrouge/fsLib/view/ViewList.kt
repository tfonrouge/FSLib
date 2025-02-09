package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.common.ICommonContainer
import com.fonrouge.fsLib.config.ConfigDataItem
import com.fonrouge.fsLib.config.ConfigDataItem.Companion.configDataItemMap
import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.config.ConfigViewItem.Companion.configViewItemMap
import com.fonrouge.fsLib.config.ConfigViewList
import com.fonrouge.fsLib.lib.iconCrud
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.CrudTask
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.State
import com.fonrouge.fsLib.tabulator.NavbarTabulator
import com.fonrouge.fsLib.tabulator.TabulatorMenuItem
import com.fonrouge.fsLib.tabulator.TabulatorViewList
import com.fonrouge.fsLib.tabulator.menuItem
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
abstract class ViewList<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<MID>, MID : Any>(
    final override val configView: ConfigViewList<CC, T, ID, out ViewList<CC, T, ID, FILT, MID>, *, FILT, MID>,
    configDataItem: ConfigDataItem<ICommonContainer<T, ID, *>, T, ID, *, *>? = null,
    configViewItem: ConfigViewItem<ICommonContainer<T, ID, FILT>, T, ID, *, *, FILT>? = null,
    periodicUpdateDataView: Boolean? = null,
    var editable: (() -> Boolean) = { true },
) : ViewDataContainer<CC, T, ID, FILT>(
    configViewContainer = configView,
) {
    var allowInstallUpdate: Boolean = true

    /**
     * Represents a potentially nullable configuration data item used within a `ViewList`.
     * This variable encapsulates an instance of the `ConfigDataItem` class, which manages
     * the relationship between the front-end view and the back-end service calls for a
     * specific container type.
     *
     * When accessed, this variable lazily resolves its value based on the current
     * configuration's common container type and its corresponding entry in the
     * `configDataItemMap`. The mapping uses dynamically resolved names derived from the
     * common container's class, enabling the reuse of configuration items across the application.
     *
     * The type parameters specify the structure and constraints of the data, including:
     *
     * - The type of the container handling the data (`ICommonContainer`).
     * - The type of individual data items (`T`).
     * - The type of identifiers used for these items (`ID`).
     *
     * This property is managed with custom getter logic to allow dynamic resolution and
     * extensibility for various container configurations. If not already initialized, it
     * attempts a lookup in `configDataItemMap` using a name derived from the `commonContainer`
     * type. The result is cast to the appropriate type for use.
     */
    var configDataItem: ConfigDataItem<ICommonContainer<T, ID, *>, T, ID, *, *>? =
        configDataItem
        get() {
            if (field != null) return field
            val commonClassName = configView.configData.commonContainer::class.simpleName
            val name = if (commonClassName?.startsWith("Common") == true) {
                commonClassName.replaceFirst("Common", "ConfigDataItem")
            } else {
                "ConfigDataItem${configView.configData.commonContainer.itemKClass.js.name}"
            }
            return configDataItemMap[name]?.unsafeCast<ConfigDataItem<ICommonContainer<T, ID, *>, T, ID, *, *>>()
        }

    /**
     * Represents a configuration view item associated with the current `ViewList`.
     *
     * This variable is used to retrieve or lazily initialize the appropriate `ConfigViewItem` for a specific
     * type of data container. When accessed, it attempts to derive the corresponding view item class name
     * based on the current `configView`, and matches it with a preloaded configuration from the
     * `configViewItemMap`. If no configuration is found, it defaults to null.
     *
     * Specific logic is used to adapt names between "ViewList" and "ViewItem" conventions, ensuring compatibility
     * with the associated `ConfigViewItem`.
     */
    var configViewItem: ConfigViewItem<ICommonContainer<T, ID, FILT>, T, ID, *, *, FILT>? =
        configViewItem
        get() {
            if (field != null) return field
            val viewClassName = configView.viewKClass.simpleName!!
            val name = if (viewClassName.startsWith("ViewList")) {
                viewClassName.replaceFirst("ViewList", "ViewItem")
            } else {
                "ViewItem${configView.configData.commonContainer.itemKClass.js.name}"
            }
            return configViewItemMap[name]?.unsafeCast<ConfigViewItem<ICommonContainer<T, ID, FILT>, T, ID, *, *, FILT>>()
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

    open fun columnDefinitionList(): List<ColumnDefinition<T>> = listOf()

    //    val columnList: List<ColumnDefinition<T>> get() = listOfNotNull(rowSelectedColumn) + columnDefinitionList()
    var masterViewItem: ViewItem<out ICommonContainer<out BaseDoc<MID>, MID, *>, out BaseDoc<MID>, MID, *>? = null
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
        configViewItem: ConfigViewItem<ICommonContainer<T, ID, FILT>, T, ID, *, *, FILT>? = this.configViewItem,
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
                    configViewItem.configData.confirmDeleteView(item, apiFilter = apiFilter) {
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
        val configViewItem = configViewItem
        val menu = mutableListOf<TabulatorMenuItem>()
        with(menu) {
            val labelId = configViewItem?.configData?.commonContainer?.labelIdFunc?.invoke(item) ?: ""
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
                it.toJs(tabulator, tabulator::translate, configView.configData.commonContainer.itemKClass)
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

    override val label: String get() = configView.configData.commonContainer.labelList

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
