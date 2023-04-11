package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.StateItem
import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.layout.centeredMessage
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.CrudAction
import com.fonrouge.fsLib.model.ItemResponse
import com.fonrouge.fsLib.model.base.BaseDoc
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
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.w3c.dom.events.MouseEvent
import web.prompts.confirm

@Suppress("unused")
abstract class ViewItem<T : BaseDoc<U>, U : Any>(
    override val configView: ConfigViewItem<T, out ViewItem<T, U>, *, U>,
    periodicUpdateDataView: Boolean? = null,
    editable: Boolean = true,
    icon: String? = null,
) : ViewDataContainer<T>(
    configView = configView,
    editable = editable,
    icon = icon,
) {
    /**
     * Observable that holds data for the [ViewItem]
     */
    internal var data: ObservableValue<ItemResponse<T>?> = ObservableValue(null)
    val item: T? get() = data.value?.item
    var buttonCancel: Button? = null
    var buttonAccept: Button? = null
    var state: String? = null

    init {
        data.subscribe {
            it?.item?.let { item ->
                labelBanner = label
                formPanel?.setData(item)
            }
            onChangeDataContainer(it)
        }
    }

    var disableEdit: Boolean = false
    var formPanel: FormPanel<T>? = null

    val labelId get() = configView.labelIdFunc?.let { it(item) }

    //    var itemId: U? = null
    var noBackButton = false

    var onAcceptButtonClick: (Button.(MouseEvent) -> Unit)? = null

    /**
     * Set to true if periodic update of [data] is allowed
     */
    final override var periodicUpdateDataView: Boolean? = periodicUpdateDataView
        get() = field ?: KVWebManager.periodicUpdateDataViewItem

    /**
     * Performs an API call to an upsert action on the backend,
     * requires [formPanel] and checks validity before the API request
     *
     * @param block optional, executes with the API result [ItemResponse] as parameter
     */
    fun acceptUpsertAction(
        block: ((ItemResponse<T>) -> Unit)? = {
            navButtonCancel?.disabled = true
            navButtonAccept?.disabled = true
            buttonCancel?.disabled = true
            buttonAccept?.disabled = true
//            backCloseAction()
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
        val crudAction = urlParams?.crudAction
        formPanel?.let { formPanel ->
            if (crudAction != null && crudAction in arrayOf(CrudAction.Create, CrudAction.Update)) {
                if (formPanel.validate()) {
                    configView.callItemService(
                        crudAction = crudAction,
                        callType = StateItem.CallType.Action,
                        itemId = encodedId(),
                        item = dataFormBeforeApiCall(formPanel.getData()),
                        contextDataUrl = urlParams?.contextDataUrl
                    ) { itemResponse ->
                        block?.let { it(itemResponse) }
                        itemResponse
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

    @OptIn(InternalSerializationApi::class)
    fun backCloseAction(confirmCancel: Boolean = false) {
        var proceedClose = true
        if (confirmCancel && formPanel != null) {
            val s1 = formPanel?.getData()?.let {
                Json.encodeToString(configView.itemKClass.serializer(), dataFormBeforeApiCall(it))
            }
            val s2 = item?.let { Json.encodeToString(configView.itemKClass.serializer(), it) }
            if (s1 != s2) {
                proceedClose = confirm("Cancel and forget current changes ?")
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
    open fun dataFormBeforeApiCall(item: T): T {
        return item
    }

    open fun Container.displayDefault(urlParams: UrlParams?) {
        centeredMessage("no CRUD action ...")
    }

    private fun Container.displayForm(crudAction: CrudAction) {
        formPanel = pageItemBody()
        if (urlParams?.actionUpsert != true) {
            formPanel?.form?.fields?.forEach { entry ->
                entry.value.disabled = true
            }
        }
        flexPanel(direction = FlexDirection.ROW, justify = JustifyContent.CENTER, spacing = 20) {
            marginTop = 1.em
            if (urlParams?.actionUpsert == true) {
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
        when (crudAction) {
            CrudAction.Create -> {
                item?.let {
                    formPanel?.setData(it)
                }
            }

            CrudAction.Read -> {
                item?.let { formPanel?.setData(it) }
                installUpdate(false)
            }

            CrudAction.Update -> {
                item?.let {
                    labelBanner = label
                    formPanel?.setData(it)
                }
            }

            else -> {}
        }

        onAfterDisplayForm(crudAction)
    }

    /**
     * Calls after form panel is displayed and data is assigned (formPanel.setData)
     */
    open fun onAfterDisplayForm(crudAction: CrudAction) {}

    override fun Container.displayPage() {
        vPanel(className = "showItem") {
            flexPanel(direction = FlexDirection.COLUMN, spacing = 10) {
                if (!noPageBanner) {
                    pageBanner()
                }
                urlParams?.crudAction?.let { crudAction ->
                    configView.callItemService(
                        crudAction = crudAction,
                        callType = StateItem.CallType.Query,
                        itemId = urlParams?.id,
                        contextDataUrl = urlParams?.contextDataUrl
                    ) { itemResponse ->
                        this@ViewItem.state = itemResponse.state
                        if (crudAction == CrudAction.Create && itemResponse.itemAlreadyOn) {
                            urlParams = UrlParams(
                                "action" to CrudAction.Update.name, "id" to encodedId(itemResponse.item?._id)
                            )
                            @Suppress("UNUSED_VARIABLE")
                            val url = (configView.url + urlParams.toString()).asDynamic()

                            @Suppress("UNUSED_VARIABLE")
                            val stateObj =
                                "{${itemResponse::class.simpleName}: \"${itemResponse.item?._id}\"}".asDynamic()
                            js("""history.replaceState(stateObj,"createToUpdate",url)""")
                        }
                        var buttonCancel: Button? = null
                        var buttonAccept: Button? = null
                        var buttonBack: Button?
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
                        val crudAction1 = urlParams?.crudAction
                        if (itemResponse.isOk && crudAction1 != null) {
                            data.value = itemResponse
                            if (crudAction1 != CrudAction.Delete) {
                                displayForm(crudAction1)
                            } else {
                                flexPanel(
                                    direction = FlexDirection.COLUMN,
                                    justify = JustifyContent.CENTER,
                                    alignContent = AlignContent.CENTER,
                                    alignItems = AlignItems.CENTER,
                                    spacing = 10
                                ) {
                                    val labelId = itemResponse.item?.let { configView.labelIdFunc?.invoke(it) }
                                    if (itemResponse.item != null && labelId != null) {
                                        div(content = "Please confirm delete of ${this@ViewItem.configView.label} '$labelId'") {
                                            fontSize = 1.5.em
                                        }
                                        flexPanel(
                                            direction = FlexDirection.ROW,
                                            justify = JustifyContent.CENTER,
                                            spacing = 20
                                        ) {
                                            buttonBack = button("Back", icon = "fa-solid fa-arrow-rotate-left") {
                                                hide()
                                                onClick {
                                                    alreadyBack = true
                                                    js("history.back()") as? Unit
                                                }
                                            }
                                            buttonCancel = button("Cancel", style = ButtonStyle.OUTLINEDANGER) {
                                                onClick {
                                                    buttonCancel?.hide()
                                                    buttonAccept?.hide()
                                                    buttonBack?.show()
                                                    Toast.warning(
                                                        message = "$crudAction1 action cancelled ...",
                                                        options = toastOptions
                                                    )
                                                }
                                            }
                                            buttonAccept =
                                                button("Accept", style = ButtonStyle.OUTLINESUCCESS) {
                                                    onClick {
                                                        configView.callItemService(
                                                            crudAction = CrudAction.Delete,
                                                            callType = StateItem.CallType.Action,
                                                            itemId = urlParams?.id,
                                                            contextDataUrl = urlParams?.contextDataUrl,
                                                        ) { itemResponse1 ->
                                                            buttonCancel?.hide()
                                                            buttonAccept?.hide()
                                                            buttonBack?.show()
                                                            if (itemResponse1.isOk) {
                                                                Toast.success(
                                                                    message = itemResponse1.msgOk
                                                                        ?: "$crudAction1 action successful ...",
                                                                    options = toastOptions
                                                                )
                                                            } else {
                                                                Toast.warning(
                                                                    message = itemResponse1.msgError
                                                                        ?: "$crudAction1 action failed ...",
                                                                    options = toastOptions
                                                                )
                                                            }
                                                            itemResponse1
                                                        }
                                                    }
                                                }
                                        }
                                    } else {
                                        div(content = "Error on Delete Query: item or item id reference null ...") {
                                            fontSize = 1.5.em
                                        }
                                        flexPanel(
                                            direction = FlexDirection.ROW,
                                            justify = JustifyContent.CENTER,
                                            spacing = 20
                                        ) {
                                            button("Back", icon = "fa-solid fa-arrow-rotate-left").onClick {
                                                alreadyBack = true
                                                js("history.back()") as? Unit
                                            }
                                        }
                                    }
                                }
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
                                    buttonBack = button("Back", icon = "fa-solid fa-arrow-rotate-left") {
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
                } ?: displayDefault(urlParams)
            }
        }
    }

    fun encodedId(_id: U? = item?._id): String {
        return configView.encodedId(_id = _id)
    }

    override val label: String
        get() {
            return "${configView.label}: ${configView.labelIdFunc?.invoke(item) ?: " < no - item > "}"
        }

    open fun onChangeDataContainer(itemResponse: ItemResponse<T>?) {

    }

    abstract fun Container.pageItemBody(): FormPanel<T>?

    override suspend fun dataUpdate() {
        urlParams?.crudAction?.let { crudAction ->
            configView.callItemService(
                crudAction = crudAction,
                callType = StateItem.CallType.Query,
                itemId = encodedId(),
                contextDataUrl = urlParams?.contextDataUrl
            ) { itemResponse ->
                data.value = itemResponse
                itemResponse
            }
        }
    }
}
