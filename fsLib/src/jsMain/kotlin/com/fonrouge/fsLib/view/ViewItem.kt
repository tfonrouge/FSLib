package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.common.ICommonContainer
import com.fonrouge.fsLib.common.callItemService
import com.fonrouge.fsLib.common.confirmDeleteView
import com.fonrouge.fsLib.common.getItemState
import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.layout.centeredMessage
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.lib.toEncodedUrlString
import com.fonrouge.fsLib.lib.toast
import com.fonrouge.fsLib.model.apiData.CallType
import com.fonrouge.fsLib.model.apiData.CrudTask
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import com.fonrouge.fsLib.model.state.SimpleState
import com.fonrouge.fsLib.model.state.State
import io.kvision.core.*
import io.kvision.form.FormPanel
import io.kvision.html.Button
import io.kvision.html.ButtonStyle
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.panel.flexPanel
import io.kvision.panel.vPanel
import io.kvision.state.ObservableValue
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import io.kvision.utils.em
import kotlinx.browser.window
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.w3c.dom.events.MouseEvent
import web.prompts.confirm

/**
 * Represents a view item with various configurations, UI components, and event handlers.
 *
 * @param configView The configuration view object associated with the view item.
 * @param itemObservable Observes changes in the item for reactivity or binding.
 * @param item The primary item data model associated with the view.
 * @param origSerialized Original serialized form of the item, used for comparison or rollback.
 * @param buttonBack The back button component in the UI.
 * @param buttonCancel The cancel button component in the UI.
 * @param buttonAccept The accept button component in the UI.
 * @param formPanel The form panel component used for displaying or editing item data.
 * @param labelId Label or identifier displayed in the UI for the item.
 * @param noBackButton Flag indicating whether the back button should be hidden or not.
 * @param onAcceptButtonClick Event handler invoked when the accept button is clicked.
 * @param periodicUpdateDataView Data view for maintaining periodic updates.
 * @param label The primary label associated with the view item.
 */
@Suppress("unused")
abstract class ViewItem<out CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
    final override val configView: ConfigViewItem<CC, T, ID, out ViewItem<CC, T, ID, FILT>, FILT, *>,
    periodicUpdateDataView: Boolean? = null,
    private var debug: Boolean = false
) : ViewDataContainer<CC, T, ID, FILT>(
    configViewContainer = configView,
) {
    /**
     * Observable that holds the [ItemState] for the [ViewItem]
     */
    val itemObservable: ObservableValue<T?> = ObservableValue(null)

    /**
     * Helper to get the item property from the [ItemState]
     */
    var item: T?
        get() = itemObservable.value
        set(value) {
            itemObservable.value = value
        }
    private var origSerialized: String? = null
    var buttonBack: Button? = null
    var buttonCancel: Button? = null
    var buttonAccept: Button? = null

    init {
        itemObservable.subscribe {
            it?.let { item ->
                if (debug) {
                    console.warn("itemObservable.subscribe:", item)
                }
                formPanel.setData(item)
                onChangeItemObservable(it)
            }
        }
    }

    var formPanel: FormPanel<T> = FormPanel(serializer = configView.commonContainer.itemSerializer)

    val labelId get() = configView.commonContainer.labelId(item)

    //    var itemId: U? = null
    var noBackButton = false

    var onAcceptButtonClick: (Button.(MouseEvent) -> Unit)? = null

    /**
     * Set to true if periodic update of [itemObservable] is allowed
     */
    final override var periodicUpdateDataView: Boolean? = periodicUpdateDataView
        get() = field ?: KVWebManager.periodicUpdateDataViewItem

    /**
     * Performs an API call to an upsert action on the backend,
     * requires [formPanel] and checks validity before the API request
     *
     * @param block optional, executes with the API result [ItemState] as parameter
     */
    fun acceptUpsertAction(
        block: ((ItemState<T>) -> Unit)? = {
            navButtonCancel?.hide()
            navButtonAccept?.hide()
            navButtonBack?.show()
            buttonCancel?.hide()
            buttonAccept?.hide()
            buttonBack?.show()
            val toastOptions = ToastOptions(
                callback = { backCloseAction() },
                close = true,
                stopOnFocus = true
            )
            if (it.hasError.not()) {
                Toast.info(
                    message = if (it.noDataModified == true) "No data was modified ..." else it.msgOk
                        ?: "info...",
                    options = toastOptions
                )
            } else {
                Toast.warning(
                    message = it.msgError ?: "!",
                    options = toastOptions
                )
            }
        }
    ) {
        val crudAction = crudTask
        if (crudAction != null && crudAction in arrayOf(CrudTask.Create, CrudTask.Update)) {
            if (formPanel.validate()) {
                val data = transformData(formPanel.getData())
                val simpleState = formPanelValidate(data)
                if (simpleState.state == State.Ok) {
                    configView.commonContainer.callItemService(
                        apiItemFun = configView.apiItemFun,
                        crudTask = crudAction,
                        callType = CallType.Action,
                        id = item?._id,
                        item = data,
                        orig = origSerialized?.let {
                            Json.decodeFromString(
                                deserializer = configView.commonContainer.itemSerializer,
                                string = it
                            )
                        },
                        apiFilter = apiFilter,
                    ) { itemResponse ->
                        block?.invoke(itemResponse)
                        if (crudAction == CrudTask.Update && itemResponse.hasError.not()) {
                            origSerialized = Json.encodeToString(
                                serializer = configView.commonContainer.itemSerializer,
                                value = data
                            )
                        }
                        itemResponse
                    }
                } else {
                    simpleState.toast()
                }
            } else {
                Toast.warning(
                    message = "Form has incomplete data",
                    options = ToastOptions(
                        position = ToastPosition.BOTTOMRIGHT,
                        stopOnFocus = true
                    )
                )
            }
        }
    }

    /**
     * Adds a viewList to the container and optionally initializes it with custom logic.
     *
     * @param viewList The ViewList object to be added. It should extend from ICommonContainer and support an API filter implementation.
     * @param init An optional lambda function to initialize the ViewList with custom configurations or behavior.
     */
    @Suppress("unused")
    fun Container.addViewList(
        viewList: ViewList<ICommonContainer<*, *, out IApiFilter<ID>>, *, *, out IApiFilter<ID>, ID>,
        init: ((ViewList<*, *, *, *, *>).() -> Unit)? = null
    ) {
        viewList.apply { startDisplayPage() }
        viewList.masterViewItem = this@ViewItem
        init?.invoke(viewList)
    }

    fun backCloseAction(confirmCancel: Boolean = false) {
        var proceedClose = true
        if (confirmCancel) {
            try {
                val s1 = Json.encodeToString(
                    configView.commonContainer.itemSerializer,
                    transformData(formPanel.getData())
                )
                val s2 =
                    item?.let { Json.encodeToString(configView.commonContainer.itemSerializer, it) }
                if (s1 != s2) {
                    proceedClose = confirm("Cancel and forget current changes ?")
                }
            } catch (e: Exception) {
                console.warn("exception = ", e)
            }
        }
        if (proceedClose) {
            if (window.history.length > 1) {
                window.history.back()
            } else {
                window.close()
            }
        }
    }

    /**
     * Override this function if you want to process the [formPanel] data content just *before*
     * to send it to the backend
     */
    open fun transformData(item: T): T {
        return item
    }

    open fun Container.displayDefault(urlParams: UrlParams?) {
        centeredMessage("no CRUD action ...")
    }

    private suspend fun Container.displayForm(crudTask: CrudTask) {
        onBeforeDisplayForm(crudTask)
        formPanel = pageItemBody()
        if (!actionUpsert) {
            formPanel.form.fields.forEach { entry ->
                entry.value.disabled = true
            }
        }
        flexPanel(direction = FlexDirection.ROW, justify = JustifyContent.CENTER, spacing = 20) {
            marginTop = 1.em
            if (actionUpsert) {
                buttonBack =
                    button(
                        text = "Back",
                        icon = "fas fa-reply",
                        style = ButtonStyle.OUTLINEPRIMARY
                    ) {
                        hide()
                        onClick {
                            backCloseAction()
                        }
                    }
                buttonCancel =
                    button(
                        text = "Cancel",
                        icon = "fas fa-xmark",
                        style = ButtonStyle.OUTLINEDANGER
                    ) {
                        onClick {
                            backCloseAction(confirmCancel = true)
                        }
                    }
                buttonAccept =
                    button(
                        text = "Accept",
                        icon = "fas fa-check",
                        style = ButtonStyle.OUTLINESUCCESS
                    ) {
                        onClick {
                            acceptUpsertAction()
                        }
                    }
            } else {
                if (!noBackButton) {
                    val histLength = window.history.length
                    val label = if (histLength > 1) "Back" else "Close"
                    button(text = label, icon = "fa-solid fa-arrow-rotate-left").onClick {
                        backCloseAction()
                    }
                }
            }
        }
        when (crudTask) {
            CrudTask.Create -> {
                item?.let {
                    formPanel.setData(it)
                }
            }

            CrudTask.Read -> {
                item?.let { formPanel.setData(it) }
                installUpdate()
            }

            CrudTask.Update -> {
//                AppScope.launch {
//                    linkBanner?.label = labelBanner(apiFilter)
//                }
                item?.let {
                    formPanel.setData(it)
                    origSerialized = Json.encodeToString(
                        configView.commonContainer.itemSerializer,
                        transformData(formPanel.getData())
                    )
                }
            }

            else -> {}
        }

        onAfterDisplayForm(crudTask)
    }

    /**
     * This method is triggered after the form associated with the given CRUD task has been displayed.
     * It can be overridden to implement additional processing or actions specific to the form display context.
     *
     * @param crudTask The CRUD operation context (e.g., Create, Read, Update, Delete) for which the form is displayed.
     */
    open fun onAfterDisplayForm(crudTask: CrudTask) {}

    /**
     * This method is triggered before the form associated with the given CRUD task is displayed.
     * It can be overridden to perform custom initialization or setup actions before the form display.
     *
     * @param crudTask The CRUD operation context (e.g., Create, Read, Update, Delete) for which the form is about to be displayed.
     */
    open suspend fun onBeforeDisplayForm(crudTask: CrudTask) {}

    override fun Container.displayPage() {
        vPanel(className = "showItem") {
            flexPanel(direction = FlexDirection.COLUMN, spacing = 10) {
                if (!noPageBanner) {
                    pageBanner()
                }
                crudTask?.let { crudAction ->
                    if (crudAction == CrudTask.Delete) {
                        item?.let { item ->
                            configView.commonContainer.confirmDeleteView(
                                apiItemFun = configView.apiItemFun,
                                item = item,
                                apiFilter = apiFilter
                            )
                        } ?: Toast.danger("${configView.commonContainer.labelItem} not valid ...")
                    } else {
                        configView.commonContainer.callItemService(
                            apiItemFun = configView.apiItemFun,
                            crudTask = crudAction,
                            callType = CallType.Query,
                            id = urlParams.id?.let {
                                Json.decodeFromString(
                                    configView.commonContainer.idSerializer,
                                    it
                                )
                            },
                            apiFilter = apiFilter
                        ) { itemResponse ->
                            if (crudAction == CrudTask.Create && itemResponse.item != null && itemResponse.itemAlreadyOn) {
                                crudTask = CrudTask.Update
                                urlParams.params["action"] = CrudTask.Update.name
                                itemResponse.item._id.let {
                                    urlParams.params.set(
                                        propertyName = "id",
                                        value = Json.encodeToString(
                                            configView.commonContainer.idSerializer,
                                            it
                                        )
                                    )
                                }
                                @Suppress("UNUSED_VARIABLE")
                                val url =
                                    (configView.url + urlParams.toEncodedUrlString()).asDynamic()

                                @Suppress("UNUSED_VARIABLE")
                                val stateObj =
                                    "{${itemResponse::class.simpleName}: \"${itemResponse.item._id}\"}".asDynamic()
                                js("""history.replaceState(stateObj,"createToUpdate",url)""")
                            }
                            var alreadyBack = false
                            val toastOptions = ToastOptions(
                                position = ToastPosition.BOTTOMRIGHT,
                                stopOnFocus = true,
                                duration = 10000,
                                close = true,
                                callback = {
                                    if (!alreadyBack) js("history.back()")
                                    Unit
                                },
                                escapeHtml = true,
                            )
                            val crudAction1 = crudTask
                            if (itemResponse.hasError.not() && crudAction1 != null) {
                                itemObservable.value = itemResponse.item
                                AppScope.launch {
                                    displayForm(crudAction1)
                                }
                            } else {
                                flexPanel(
                                    direction = FlexDirection.COLUMN,
                                    justify = JustifyContent.CENTER,
                                    alignContent = AlignContent.CENTER,
                                    alignItems = AlignItems.CENTER,
                                    spacing = 10
                                ) {
                                    div(
                                        content = "<i><b>[$crudAction1]</b></i> action denied: <b>${itemResponse.msgError}</b>",
                                        rich = true
                                    ) {
                                        fontSize = 1.5.em
                                    }
                                    flexPanel(
                                        direction = FlexDirection.ROW,
                                        justify = JustifyContent.CENTER,
                                        spacing = 20
                                    ) {
                                        button("Back", icon = "fa-solid fa-arrow-rotate-left") {
                                            onClick {
                                                alreadyBack = true
                                                js("history.back()") as? Unit
                                            }
                                        }
                                    }
                                }
                                Toast.warning(
                                    message = itemResponse.msgError
                                        ?: "$crudAction1 action denied ...",
                                    options = toastOptions
                                )
                            }
                            itemResponse
                        }
                    }
                } ?: displayDefault(urlParams)
            }
        }
    }

    override val label: String
        get() = "${configView.label}: ${configView.commonContainer.labelId(item)}"

    fun encodeId(id: ID? = item?._id): String? {
        return id?.let { Json.encodeToString(configView.commonContainer.idSerializer, id) }
    }

    /**
     * Validates the form panel data.
     *
     * @param data The data to be validated.
     * @return A SimpleState object indicating the validation result.
     */
    open fun formPanelValidate(data: T?): SimpleState =
        SimpleState(
            isOk = data != null,
            msgError = "${configView.commonContainer.labelItem} is null"
        )

    /**
     * Called when the [ItemState] value changes
     */
    open fun onChangeItemObservable(item: T) {}

    abstract fun Container.pageItemBody(): FormPanel<T>

    final override fun dataUpdate() {
        if (crudTask == CrudTask.Read) {
            item?._id?.let { id ->
                configView.commonContainer.getItemState(
                    apiItemFun = configView.apiItemFun,
                    apiItem = configView.commonContainer.apiItemQueryRead(id = id, apiFilter = apiFilter),
                ) {
                    itemObservable.value = it.item
                }
            }
        }
    }
}
