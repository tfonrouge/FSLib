package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.common.ICommonContainer
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
 * Abstract class representing a view item within a common container.
 *
 * @param CC The common container type.
 * @param T The item type.
 * @param ID The item ID type.
 * @param FILT The filter type.
 * @param configView The configuration for the view item.
 * @param periodicUpdateDataView Flag to enable or disable periodic updates.
 * @param icon The icon representation for the view item.
 */
@Suppress("unused")
abstract class ViewItem<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
    final override val configView: ConfigViewItem<CC, T, ID, out ViewItem<CC, T, ID, FILT>, *, FILT>,
    periodicUpdateDataView: Boolean? = null,
    icon: String? = null,
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
            AppScope.launch {
                updateLabelBanner()
            }
            it?.let { item ->
                formPanel?.setData(item)
                onChangeItemObservable(it)
            }
        }
    }

    var disableEdit: Boolean = false
    var formPanel: FormPanel<T>? = null

    val labelId get() = configView.configData.commonContainer.labelIdFunc(item)

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
        val crudAction = urlParams?.crudTask
        formPanel?.let { formPanel ->
            if (crudAction != null && crudAction in arrayOf(CrudTask.Create, CrudTask.Update)) {
                if (formPanel.validate()) {
                    val data = transformData(formPanel.getData())
                    val simpleState = formPanelValidate(data)
                    if (simpleState.state != State.Error) {
                        configView.configData.callItemService(
                            crudTask = crudAction,
                            callType = CallType.Action,
                            id = item?._id,
                            item = data,
                            orig = origSerialized?.let {
                                Json.decodeFromString(
                                    deserializer = configView.configData.commonContainer.itemSerializer,
                                    string = it
                                )
                            },
                            apiFilter = apiFilter,
                        ) { itemResponse ->
                            block?.invoke(itemResponse)
                            if (crudAction == CrudTask.Update && itemResponse.hasError.not()) {
                                origSerialized = Json.encodeToString(
                                    serializer = configView.configData.commonContainer.itemSerializer,
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
    }

    fun backCloseAction(confirmCancel: Boolean = false) {
        var proceedClose = true
        if (confirmCancel && formPanel != null) {
            try {
                val s1 = formPanel?.getData()?.let {
                    Json.encodeToString(
                        configView.configData.commonContainer.itemSerializer,
                        transformData(it)
                    )
                }
                val s2 =
                    item?.let { Json.encodeToString(configView.configData.commonContainer.itemSerializer, it) }
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
        if (urlParams?.actionUpsert != true) {
            formPanel?.form?.fields?.forEach { entry ->
                entry.value.disabled = true
            }
        }
        flexPanel(direction = FlexDirection.ROW, justify = JustifyContent.CENTER, spacing = 20) {
            marginTop = 1.em
            if (urlParams?.actionUpsert == true) {
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
                    formPanel?.setData(it)
                }
            }

            CrudTask.Read -> {
                item?.let { formPanel?.setData(it) }
                installUpdate(false)
            }

            CrudTask.Update -> {
//                AppScope.launch {
//                    linkBanner?.label = labelBanner(apiFilter)
//                }
                item?.let {
                    formPanel?.setData(it)
                    origSerialized = formPanel?.getData()?.let {
                        Json.encodeToString(
                            configView.configData.commonContainer.itemSerializer,
                            transformData(it)
                        )
                    }
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
                urlParams?.crudTask?.let { crudAction ->
                    if (crudAction == CrudTask.Delete) {
                        item?.let { item ->
                            configView.configData.confirmDeleteView(item, apiFilter = apiFilter)
                        } ?: Toast.danger("${configView.configData.commonContainer.labelItem} not valid ...")
                    } else {
                        configView.configData.callItemService(
                            crudTask = crudAction,
                            callType = CallType.Query,
                            id = urlParams?.id?.let {
                                Json.decodeFromString(
                                    configView.configData.commonContainer.idSerializer,
                                    it
                                )
                            },
                            apiFilter = apiFilter
                        ) { itemResponse ->
                            if (crudAction == CrudTask.Create && itemResponse.item != null && itemResponse.itemAlreadyOn) {
                                urlParams?.params?.set("action", CrudTask.Update.name)
                                itemResponse.item._id.let {
                                    urlParams?.params?.set(
                                        propertyName = "id",
                                        value = Json.encodeToString(
                                            configView.configData.commonContainer.idSerializer,
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
                            val crudAction1 = urlParams?.crudTask
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
        get() = "${configView.label}: ${configView.configData.commonContainer.labelIdFunc(item)}"

    fun encodeId(id: ID? = item?._id): String? {
        return id?.let { Json.encodeToString(configView.configData.commonContainer.idSerializer, id) }
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
            msgError = "${configView.configData.commonContainer.labelItem} is null"
        )

    /**
     * Called when the [ItemState] value changes
     */
    open fun onChangeItemObservable(item: T) {}

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
