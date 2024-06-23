package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.config.ICommonContainer
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
import kotlinx.serialization.json.Json
import org.w3c.dom.events.MouseEvent
import web.prompts.confirm

@Suppress("unused")
abstract class ViewItem<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
    final override val configView: ConfigViewItem<CC, T, ID, out ViewItem<CC, T, ID, FILT>, *, FILT>,
    periodicUpdateDataView: Boolean? = null,
    editable: (() -> Boolean) = { true },
    icon: String? = null,
) : ViewDataContainer<CC, T, ID, FILT>(
    configViewContainer = configView,
    editable = editable,
    icon = icon,
) {
    /**
     * Observable that holds the [ItemState] for the [ViewItem]
     */
    var itemStateObservableValue: ObservableValue<ItemState<T>> = ObservableValue(ItemState())

    /**
     * Helper to get the item property from the [ItemState]
     */
    val item: T? get() = itemStateObservableValue.value.item
    var buttonBack: Button? = null
    var buttonCancel: Button? = null
    var buttonAccept: Button? = null

    init {
        itemStateObservableValue.subscribe {
            it.item?.let { item ->
                labelBanner = label
                formPanel?.setData(item)
            }
            onChangeItemState(it)
        }
    }

    var disableEdit: Boolean = false
    var formPanel: FormPanel<T>? = null

    val labelId get() = configView.commonContainer.labelIdFunc(item)

    //    var itemId: U? = null
    var noBackButton = false

    var onAcceptButtonClick: (Button.(MouseEvent) -> Unit)? = null

    /**
     * Set to true if periodic update of [itemStateObservableValue] is allowed
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
            if (it.isOk) {
                Toast.info(
                    message = if (it.noDataModified == true) "No data was modified ..." else it.msgOk ?: "info...",
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
        val crudAction = urlParams?.crudTask
        formPanel?.let { formPanel ->
            if (crudAction != null && crudAction in arrayOf(CrudTask.Create, CrudTask.Update)) {
                if (formPanel.validate()) {
                    val data = formPanelGetData()
                    val simpleState = formPanelValidate(data)
                    if (simpleState.isOk) {
                        configView.callItemService(
                            crudTask = crudAction,
                            callType = CallType.Action,
                            id = item?._id,
                            item = data?.let { transformData(it) },
                            apiFilter = apiFilter,
                        ) { itemResponse ->
                            block?.let { it(itemResponse) }
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
    }

    fun backCloseAction(confirmCancel: Boolean = false) {
        var proceedClose = true
        if (confirmCancel && formPanel != null) {
            try {
                val s1 = formPanelGetData()?.let {
                    Json.encodeToString(configView.commonContainer.itemSerializer, transformData(it))
                }
                val s2 = item?.let { Json.encodeToString(configView.commonContainer.itemSerializer, it) }
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

    private fun Container.displayForm(crudTask: CrudTask) {
        formPanel = pageItemBody()
        if (urlParams?.actionUpsert != true) {
            formPanel?.form?.fields?.forEach { entry ->
                entry.value.disabled = true
            }
        }
        flexPanel(direction = FlexDirection.ROW, justify = JustifyContent.CENTER, spacing = 20) {
            marginTop = 1.em
            if (urlParams?.actionUpsert == true) {
                buttonBack =
                    button(text = "Back", icon = "fas fa-reply", style = ButtonStyle.OUTLINEPRIMARY) {
                        hide()
                        onClick {
                            backCloseAction()
                        }
                    }
                buttonCancel =
                    button(text = "Cancel", icon = "fas fa-xmark", style = ButtonStyle.OUTLINEDANGER) {
                        onClick {
                            backCloseAction(confirmCancel = true)
                        }
                    }
                buttonAccept =
                    button(text = "Accept", icon = "fas fa-check", style = ButtonStyle.OUTLINESUCCESS) {
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
                    formPanel?.setData(it)
                }
            }

            CrudTask.Read -> {
                item?.let { formPanel?.setData(it) }
                installUpdate(false)
            }

            CrudTask.Update -> {
                item?.let {
                    labelBanner = label
                    formPanel?.setData(it)
                }
            }

            else -> {}
        }

        onAfterDisplayForm(crudTask)
    }

    /**
     * Calls after form panel is displayed and data is assigned (formPanel.setData)
     */
    open fun onAfterDisplayForm(crudTask: CrudTask) {}

    override fun Container.displayPage() {
        vPanel(className = "showItem") {
            flexPanel(direction = FlexDirection.COLUMN, spacing = 10) {
                if (!noPageBanner) {
                    pageBanner()
                }
                urlParams?.crudTask?.let { crudAction ->
                    if (crudAction == CrudTask.Delete) {
                        item?.let { item ->
                            confirmDeleteView(item, configView, apiFilter = apiFilter)
                        } ?: Toast.danger("${configView.commonContainer.labelItem} not valid ...")
                    } else {
                        configView.callItemService(
                            crudTask = crudAction,
                            callType = CallType.Query,
                            id = urlParams?.id?.let {
                                Json.decodeFromString(
                                    configView.commonContainer.idSerializer,
                                    it
                                )
                            },
                            apiFilter = apiFilter
                        ) { itemResponse ->
                            if (crudAction == CrudTask.Create && itemResponse.itemAlreadyOn) {
                                urlParams?.params?.set("action", CrudTask.Update.name)
                                itemResponse.item?._id?.let {
                                    urlParams?.params?.set(
                                        propertyName = "id",
                                        value = Json.encodeToString(
                                            configView.commonContainer.idSerializer,
                                            it
                                        )
                                    )
                                }
                                @Suppress("UNUSED_VARIABLE")
                                val url = (configView.url + urlParams.toEncodedUrlString()).asDynamic()

                                @Suppress("UNUSED_VARIABLE")
                                val stateObj =
                                    "{${itemResponse::class.simpleName}: \"${itemResponse.item?._id}\"}".asDynamic()
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
                            val crudAction1 = urlParams?.crudTask
                            if (itemResponse.isOk && crudAction1 != null) {
                                itemStateObservableValue.value = itemResponse
                                displayForm(crudAction1)
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
                                    message = itemResponse.msgError ?: "$crudAction1 action denied ...",
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
        get() = "${configView.label}: ${configView.commonContainer.labelIdFunc(item)}"

    fun encodeId(id: ID? = item?._id): String? {
        return id?.let { Json.encodeToString(configView.commonContainer.idSerializer, id) }
    }

    /**
     * Retrieves the data from the form panel.
     *
     * @return The data from the form panel, or null if the form panel is null.
     */
    open fun formPanelGetData(): T? = formPanel?.getData()

    /**
     * Validates the form panel data.
     *
     * @param data The data to be validated.
     * @return A SimpleState object indicating the validation result.
     */
    open fun formPanelValidate(data: T?): SimpleState =
        SimpleState(isOk = data != null, msgError = "${configView.commonContainer.labelItem} is null")

    /**
     * Called when the [ItemState] value changes
     */
    open fun onChangeItemState(itemState: ItemState<T>) {}

    abstract fun Container.pageItemBody(): FormPanel<T>?

    final override suspend fun dataUpdate() {
        /*
                urlParams?.crudTask?.let { crudAction ->
                    configView.callItemService(
                        crudTask = crudAction,
                        callType = ApiItem.CallType.Query,
                        id = item?._id,
                        apiFilter = apiFilter.value
                    ) { itemResponse ->
                        data.value = itemResponse
                        itemResponse
                    }
                }
        */
    }
}
