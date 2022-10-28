package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.StateItem
import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.layout.centeredMessage
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.CrudAction
import com.fonrouge.fsLib.model.ItemResponse
import com.fonrouge.fsLib.model.base.BaseModel
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
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.w3c.dom.events.MouseEvent

@Suppress("unused")
abstract class ViewItem<T : BaseModel<U>, U : Any>(
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

    //    var itemId: U? = null
    var noBackButton = false
    var noPageBanner = false
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
    @Suppress("MemberVisibilityCanBePrivate")
    fun acceptUpsertAction(block: ((ItemResponse<T>) -> Unit)? = null) {
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
                    ) { itemContainer ->
                        block?.let { it(itemContainer) }
                        itemContainer
                    }
                } else {
                    Toast.warning(
                        message = "Form has incomplete data",
                        options = ToastOptions(
                            positionClass = ToastPosition.BOTTOMRIGHT,
                            progressBar = true,
                        )
                    )
                }
            }
        }
    }

    internal fun clickCancel() = js("history.back()") as? Unit

    /**
     * Override this function if you want to process the [formPanel] data content just *before*
     * to send the data to the backend
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
                button("Cancel", style = ButtonStyle.OUTLINEDANGER) {
                    onClick {
                        clickCancel()
                    }
                }
                button("Accept", style = ButtonStyle.OUTLINESUCCESS) {
//                                marginLeft = 10.px
                    onClick {
                        acceptUpsertAction {
                            if (it.isOk) {
                                Toast.success("Info", it.msgOk)
                            } else {
                                Toast.warning("!", it.msgError)
                            }
                            js("history.back()")
                            Unit
                        }
                    }
                }
            } else {
                if (!noBackButton) {
                    button("Back", icon = "fa-solid fa-arrow-rotate-left").onClick {
                        js("history.back()") as? Unit
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
    }

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
                    ) { itemContainer ->
                        if (crudAction == CrudAction.Create && itemContainer.itemAlreadyOn) {
                            urlParams = UrlParams(
                                "action" to CrudAction.Update.name, "id" to encodedId(itemContainer.item?._id)
                            )
                            @Suppress("UNUSED_VARIABLE")
                            val url = (configView.url + urlParams.toString()).asDynamic()

                            @Suppress("UNUSED_VARIABLE")
                            val stateObj =
                                "{${itemContainer::class.simpleName}: \"${itemContainer.item?._id}\"}".asDynamic()
                            js("""history.replaceState(stateObj,"createToUpdate",url)""")
                        }
                        var buttonCancel: Button? = null
                        var buttonAccept: Button? = null
                        var buttonBack: Button?
                        var alreadyBack = false
                        val toastOptions = ToastOptions(
                            positionClass = ToastPosition.BOTTOMFULLWIDTH,
                            progressBar = true,
                            closeDuration = 10,
                            extendedTimeOut = 20,
                            closeButton = true,
                            onHidden = {
                                if (!alreadyBack) js("history.back()")
                                Unit
                            },
                            escapeHtml = true,
                            closeHtml = "<button type=\"button\">Close</button>"
                        )
                        val crudAction1 = urlParams?.crudAction
                        if (itemContainer.isOk && crudAction1 != null) {
                            data.value = itemContainer
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
                                    val id = itemContainer.item?.let { configView.labelId?.invoke(it) }
                                        ?: itemContainer.item?._id
                                    div(content = "Please confirm delete of ${this@ViewItem.configView.label} '$id'") {
                                        fontSize = 1.5.em
                                    }
                                    flexPanel(
                                        direction = FlexDirection.ROW,
                                        justify = JustifyContent.CENTER,
                                        spacing = 20
                                    ) {
                                        buttonBack = button("Back", icon = "fa-solid fa-arrow-rotate-left") {
                                            hide()
                                        }.onClick {
                                            alreadyBack = true
                                            js("history.back()") as? Unit
                                        }
                                        buttonCancel = button("Cancel", style = ButtonStyle.OUTLINEDANGER).onClick {
                                            buttonCancel?.hide()
                                            buttonAccept?.hide()
                                            buttonBack?.show()
                                            Toast.warning(
                                                message = "$crudAction1 action cancelled ...",
                                                title = "$crudAction1 cancelled",
                                                options = toastOptions
                                            )
                                        }
                                        buttonAccept = button("Accept", style = ButtonStyle.OUTLINESUCCESS).onClick {
                                            configView.callItemService(
                                                crudAction = CrudAction.Delete,
                                                callType = StateItem.CallType.Action,
                                                itemId = urlParams?.id,
                                                contextDataUrl = urlParams?.contextDataUrl,
                                            ) { itemContainer ->
                                                buttonCancel?.hide()
                                                buttonAccept?.hide()
                                                buttonBack?.show()
                                                if (itemContainer.isOk) {
                                                    Toast.success(
                                                        message = itemContainer.msgOk
                                                            ?: "$crudAction1 action successful ...",
                                                        title = "$crudAction1 success",
                                                        options = toastOptions
                                                    )
                                                } else {
                                                    Toast.warning(
                                                        message = itemContainer.msgError
                                                            ?: "$crudAction1 action failed ...",
                                                        title = "$crudAction1 failed",
                                                        options = toastOptions
                                                    )
                                                }
                                                itemContainer
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            vPanel(className = "showItem") {
                                centeredMessage("$crudAction1 action denied ...")
                            }
                            Toast.warning(
                                message = itemContainer.msgError ?: "$crudAction1 action denied ...",
                                title = "Action denied",
                                options = toastOptions
                            )
                        }
                        itemContainer
                    }
                } ?: displayDefault(urlParams)
            }
        }
    }

    @OptIn(InternalSerializationApi::class)
    fun encodedId(_id: U? = item?._id): String {
        return _id?.let {
            configView.idKClass?.let { kClass ->
                Json.encodeToString(kClass.serializer(), it)
            }
        } ?: JSON.stringify(_id)
    }

    override val label: String
        get() {
            return "${configView.label}: ${configView.labelId?.invoke(item) ?: " < no - item > "}"
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
            ) { itemContainer ->
                data.value = itemContainer
                itemContainer
            }
        }
    }
}
