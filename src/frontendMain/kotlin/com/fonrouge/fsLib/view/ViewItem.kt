package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.StateItem
import com.fonrouge.fsLib.apiLib.KVWebManager
import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.layout.centeredMessage
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.CrudAction
import com.fonrouge.fsLib.model.ItemContainer
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
import org.w3c.dom.events.MouseEvent

@Suppress("unused")
abstract class ViewItem<T : BaseModel<U>, U>(
    override val configView: ConfigViewItem<T, out ViewItem<T, U>, *, U>,
    repeatRefreshView: Boolean? = null,
    editable: Boolean = true,
    icon: String? = null,
) : ViewDataContainer<T>(
    configView = configView,
    editable = editable,
    icon = icon,
) {
    var dataContainer: ObservableValue<ItemContainer<T>?> = ObservableValue(null)

    init {
        dataContainer.subscribe {
            it?.item?.let { item ->
                linkBanner?.label = getCaption()
                formPanel?.setData(item)
            }
            onChangeDataContainer(it)
        }
    }

    var disableEdit: Boolean = false
    var formPanel: FormPanel<T>? = null
    var itemId: U? = null
    var noBackButton = false
    var noPageBanner = false
    var onAcceptButtonClick: (Button.(MouseEvent) -> Unit)? = null
    override var repeatUpdateView: Boolean? = repeatRefreshView
        get() = field ?: KVWebManager.refreshViewItemPeriodic

    fun addContext(urlParams: UrlParams) {
        dataContainer.value?.let { itemContainer ->
            urlParams.add("contextClass" to (itemContainer.item?.let { it::class.simpleName } ?: ""))
            urlParams.add("contextId" to JSON.stringify(itemContainer.item?._id))
        }
    }

    fun callUpdateItemService() {
        if (urlParams?.actionUpsert == true) {
            formPanel?.getData()?.let {
                configView.callItemService(
                    crudAction = CrudAction.Update,
                    callType = StateItem.CallType.Action,
                    itemId = JSON.stringify(itemId),
                    item = it,
                    contextDataUrl = urlParams?.contextDataUrl
                ) { itemContainer ->
                    dataContainer.value = itemContainer
                }
            }
        }
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
                        js("history.back()") as? Unit
                    }
                }
                button("Accept", style = ButtonStyle.OUTLINESUCCESS) {
//                                marginLeft = 10.px
                    onClick {
                        if (formPanel?.validate() == true) {
                            configView.callItemService(
                                crudAction = crudAction,
                                callType = StateItem.CallType.Action,
                                itemId = JSON.stringify(itemId),
                                item = formPanel?.getData(),
                                contextDataUrl = urlParams?.contextDataUrl
                            ) {
                                if (it.isOk) {
                                    Toast.success("Info", "Operation successful")
                                } else {
                                    Toast.warning("!", "Operation error: ${it.msgError}")
                                }
                                js("history.back()") as? Unit
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
                dataContainer.value?.item?.let {
                    formPanel?.setData(it)
                }
            }

            CrudAction.Read -> {
                dataContainer.value?.item?.let { formPanel?.setData(it) }
                installUpdate(false)
            }

            CrudAction.Update -> {
                dataContainer.value?.item?.let {
                    linkBanner?.label = getCaption()
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
                                "action" to CrudAction.Update.name, "id" to JSON.stringify(itemContainer.item?._id)
                            )
                            console.warn("crudAction", urlParams?.crudAction)
                            @Suppress("UNUSED_VARIABLE")
                            val url = (configView.url + urlParams.toString()).asDynamic()

                            @Suppress("UNUSED_VARIABLE")
                            val stateObj =
                                "{${itemContainer::class.simpleName}: \"${itemContainer.item?._id}\"}".asDynamic()
                            js("""history.replaceState(stateObj,"createToUpdate",url)""")
                        }
                        var buttonCancel: Button? = null
                        var buttonAccept: Button? = null
                        var buttonBack: Button? = null
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
                            itemId = itemContainer.item?._id
                            dataContainer.value = itemContainer
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
                                    div(content = "Please confirm delete of ${this@ViewItem.configView.label} '$itemId'") {
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
                    }
                } ?: displayDefault(urlParams)
            }
        }
    }

    override fun label(): String {
        return configView.labelId?.invoke(dataContainer.value?.item) ?: "<no-item>"
    }

    open fun onChangeDataContainer(itemContainer: ItemContainer<T>?) {

    }

    abstract fun Container.pageItemBody(): FormPanel<T>?

    override suspend fun dataUpdate() {
        urlParams?.crudAction?.let { crudAction ->
            configView.callItemService(
                crudAction = crudAction,
                callType = StateItem.CallType.Query,
                itemId = JSON.stringify(itemId),
                contextDataUrl = urlParams?.contextDataUrl
            ) { itemContainer ->
                dataContainer.value = itemContainer
            }
        }
    }
}
