@file:Suppress("unused")

package com.fonrouge.fsLib.layout

import com.fonrouge.fsLib.ContextDataUrl
import com.fonrouge.fsLib.config.ConfigViewList
import com.fonrouge.fsLib.model.IDataList
import com.fonrouge.fsLib.model.base.BaseModel
import com.fonrouge.fsLib.view.ViewDataContainer
import com.fonrouge.fsLib.view.ViewItem
import com.fonrouge.fsLib.view.ViewList
import io.kvision.core.Container
import io.kvision.core.onEvent
import io.kvision.panel.vPanel
import io.kvision.remote.CallAgent
import io.kvision.tabulator.*
import io.kvision.tabulator.js.Tabulator.RowComponent
import io.kvision.types.DateSerializer
import io.kvision.utils.Serialization
import io.kvision.utils.createInstance
import io.kvision.utils.em
import kotlinx.browser.window
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.overwriteWith
import kotlinx.serialization.serializer
import org.w3c.dom.events.Event
import kotlin.js.Date
import kotlin.js.json

inline fun <reified T : BaseModel<U>, E : IDataList, U : Any> Container.fsTabulator(
    configViewList: ConfigViewList<T, out ViewList<T, E, U>, E, U>,
    masterViewItem: ViewItem<*, *>,
    minToolbarSize: Boolean = true,
    noinline stateJsonFun: (ContextDataUrl.() -> Unit)? = null,
    noinline init: (TabulatorRemote<T, E>.() -> Unit)? = null
): ViewList<T, E, U> {
    val viewList = configViewList.viewFunc.js.createInstance<ViewList<T, E, U>>(null)
    viewList.masterViewItem = masterViewItem
    return fsTabulator(
        viewList = viewList,
        minToolbarSize = minToolbarSize,
        stateJsonFun = stateJsonFun,
        init = init
    )
}

@OptIn(InternalSerializationApi::class)
inline fun <reified T : BaseModel<U>, E : IDataList, U : Any> Container.fsTabulator(
    viewList: ViewList<T, E, U>,
    minToolbarSize: Boolean = true,
    noinline stateJsonFun: (ContextDataUrl.() -> Unit)? = null,
    noinline init: (TabulatorRemote<T, E>.() -> Unit)? = null
): ViewList<T, E, U> {

    viewList.configView.serviceManager.requireCall(viewList.configView.function).let {
        viewList.apiUrl = it.first
        viewList.apiMethod = it.second
    }
    viewList.apiCallAgent = CallAgent()

    viewList.stateFunction = {
        val urlParams = if (viewList.masterViewItem != null) viewList.masterViewItem?.urlParams else viewList.urlParams
        val contextDataUrl = urlParams?.contextDataUrl ?: ContextDataUrl()
        viewList.masterViewItem?.let { viewItem ->
            viewItem.item?.let {
                contextDataUrl.contextClass = viewList.masterViewItem?.configView?.itemKClass?.simpleName
                contextDataUrl.contextId = viewItem.encodedId()
            }
        }
        contextDataUrl.params = JSON.stringify(urlParams?.params)
        stateJsonFun?.let {
            contextDataUrl.json = it(contextDataUrl).let { json -> JSON.stringify(json) }
        }
        Json.encodeToString(contextDataUrl)
    }

    viewList.serializer = T::class.serializer()
    viewList.module = null
    viewList.jsonHelper = Json(from = (Serialization.customConfiguration ?: Json {
        ignoreUnknownKeys = true
        isLenient = true
    })) {
        serializersModule = SerializersModule {
            contextual(Date::class, DateSerializer)
            viewList.module?.let { this.include(it) }
        }.overwriteWith(serializersModule)
    }

    vPanel {
        viewList.navbarTabulator = toolBarList(viewList = viewList, minToolbarSize)
        viewList.tabulator = tabulatorRemote(
            serviceManager = viewList.configView.serviceManager,
            function = viewList.configView.function,
            stateFunction = viewList.stateFunction,
            serializer = viewList.serializer,
            options = TabulatorOptions(
                columns = viewList.columnDefinitionList,
//                height = if (viewList.masterViewItem == null) "calc(100vh - 30vh)" else null,
                layout = Layout.FITDATASTRETCH,
                layoutColumnsOnNewData = true,
                pagination = true,
                paginationMode = PaginationMode.REMOTE,
                paginationCounter = "rows",
                paginationSize = 10,
                paginationSizeSelector = true,
                persistenceID = viewList.configView.itemKClass.simpleName,
                persistence = json(
                    "page" to json("page" to true),
                ),
                rowContextMenu = { viewList.contextRowMenuGenerator() },
                filterMode = FilterMode.REMOTE,
                sortMode = SortMode.REMOTE,
                dataLoader = false,
//                dataLoaderLoading = "Loading.........",
                autoResize = true,
            ),
        ) {
            init?.invoke(this)
            id = viewList.urlParams?.toString()
            fontSize = 0.75.em
            onEvent {
                rowSelectionChangedTabulator = {
                    val tList = self.getSelectedData()
                    val item = tList.let {
                        if (it.isEmpty()) null else it[0]
                    }
                    viewList.updateLinks(item, tList.size)
                    viewList.onRowSelected(item)
                }
                rowClickTabulator = {
                    self.toggleSelectRow(it.detail.asDynamic()._row)
                    it.preventDefault()
                }
            }
            addAfterInsertHook {
                /*
                TODO: implement this in KVision
                 */
                jsTabulator?.on("rowMouseOver") { event: Event, row: RowComponent ->
                    if (!event.defaultPrevented) {
                        viewList.overItem = row.getData()
                        ViewDataContainer.clearStartTime()
                    }
                }
                jsTabulator?.on("menuOpened") {
                    viewList.menuOpenedState = true
                }
                jsTabulator?.on("menuClosed") {
                    viewList.menuOpenedState = false
                }
                jsTabulator?.on("tableBuilt") {
                    viewList.jsTabulatorBuilt = true
                }
                jsTabulator?.on("dataProcessing") {
                    window.setTimeout(
                        {
                            val list = viewList.selectedIdList
                            if (list?.isNotEmpty() == true) {
                                jsTabulator?.getRows("")?.firstOrNull {
                                    it.getData().asDynamic()["_id"] == list[0]
                                }?.select?.invoke()
                            }
                        },
                        0
                    )
                }
            }
        }
    }
    viewList.installUpdate(true)
    return viewList
}

fun <T : BaseModel<*>> Tabulator<T>.update(list: List<T>?) {
    if (list == null) {
        clearData()
    } else {
        val selectedId = getSelectedData().let {
            if (it.isNotEmpty()) it[0]._id else null
        }
        replaceData(list.toTypedArray())
        if (selectedId != null) {
            selectRow(selectedId)
        }
    }
}
