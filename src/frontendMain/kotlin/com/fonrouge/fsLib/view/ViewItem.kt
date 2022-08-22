package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.KPair
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
    open val onCreateDefaultValueList: List<KPair<T, *>>? = null
    var dataContainer: ObservableValue<ItemContainer<T>?> = ObservableValue(null)

    init {
        dataContainer.subscribe {
            it?.item?.let { item ->
                linkBanner?.label = getCaption()
                formPanel?.setData(item)
            }
        }
    }

    var disableEdit: Boolean = false
    var formPanel: FormPanel<T>? = null
    var itemId: U? = null
    val itemNameFunc: ((ItemContainer<T>) -> String) = { it.item?._id?.toString() ?: "<no item>" }
    var noBackButton = false
    var noPageBanner = false
    var onAcceptButtonClick: (Button.(MouseEvent) -> Unit)? = null
    override var repeatUpdateView: Boolean? = repeatRefreshView
        get() = field ?: KVWebManager.refreshViewItemPeriodic

    fun addContext(urlParams: UrlParams) {
        dataContainer.value?.let { itemContainer ->
            urlParams.add("contextClass" to itemContainer.item?.let { it::class.simpleName })
            urlParams.add("contextId" to itemContainer.item?._id)
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

    open fun Container.displayDefault(urlParams: UrlParams?) {}

    private fun Container.displayForm(crudAction: CrudAction) {
        addBeforeDisposeHook {
            handleInterval = null
            onBeforeDispose()
        }
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
                                if (it.result) {
                                    Toast.success("Info", "Operation successful")
                                } else {
                                    Toast.warning("!", "Operation error: ${it.description}")
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
                console.warn("CREATE ASSIGNING")
                dataContainer.value?.item?.let {
                    console.warn("CREATE ASSIGNING it:", it)
                    formPanel?.setData(it)
                }
//                val onCreateDefaultValue: List<KPair<T, *>>? = dataContainer.value?.onCreateDefaultValue?.let {
//                    Json.decodeFromString(it)
//                }
                val list1 = dataContainer.value?.onCreateDefaultValue
                console.warn("onCreateDefaultValue", list1)
                onCreateDefaultValueList?.forEach { kPair ->
                    formPanel?.form?.fields?.asIterable()
                        ?.firstOrNull { kPair.kProp.name == it.key }?.value?.setValue(
                            kPair.value
                        )
                }
            }

            CrudAction.Read -> {
                dataContainer.value?.item?.let { formPanel?.setData(it) }
//                dataContainer.subscribe {
//                    it?.item?.let { item ->
//                        linkBanner?.label = getCaption()
//                        formPanel?.setData(item)
//                    }
//                }
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
                val crudAction = urlParams?.crudAction
                if (crudAction != null) {
                    configView.callItemService(
                        crudAction = crudAction,
                        callType = StateItem.CallType.Query,
                        itemId = urlParams?.id,
                        contextDataUrl = urlParams?.contextDataUrl

                    ) { itemContainer ->
                        val toastOptions = ToastOptions(
                            positionClass = ToastPosition.BOTTOMFULLWIDTH,
                            progressBar = true,
                            closeDuration = 10,
                            extendedTimeOut = 20,
                            closeButton = true,
                            onHidden = {
                                js("history.back()")
                                Unit
                            },
                            escapeHtml = true,
                            closeHtml = "<button type=\"button\">Close</button>"
                        )
                        if (itemContainer.result) {
                            itemId = itemContainer.item?._id
                            dataContainer.value = itemContainer
                            if (crudAction != CrudAction.Delete) {
                                displayForm(crudAction)
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
                                        button("Cancel", style = ButtonStyle.OUTLINEDANGER).onClick {
                                            Toast.warning(
                                                message = "$crudAction action cancelled ...",
                                                title = "$crudAction cancelled",
                                                options = toastOptions
                                            )
                                        }
                                        button("Accept", style = ButtonStyle.OUTLINESUCCESS).onClick {
                                            configView.callItemService(
                                                crudAction = CrudAction.Delete,
                                                callType = StateItem.CallType.Action,
                                                itemId = urlParams?.id,
                                                contextDataUrl = urlParams?.contextDataUrl,
                                            ) { itemContainer ->
                                                if (itemContainer.result) {
                                                    Toast.success(
                                                        message = itemContainer.description
                                                            ?: "$crudAction action successful ...",
                                                        title = "$crudAction success",
                                                        options = toastOptions
                                                    )
                                                } else {
                                                    Toast.warning(
                                                        message = itemContainer.description
                                                            ?: "$crudAction action failed ...",
                                                        title = "$crudAction failed",
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
                                centeredMessage("$crudAction action denied ...")
                            }
                            Toast.warning(
                                message = itemContainer.description
                                    ?: "$crudAction action successful ...",
                                title = "$crudAction action denied",
                                options = toastOptions
                            )
                        }
                    }
                } else {
                    displayDefault(urlParams)
                }
            }
        }
    }

    override fun getName(): String? {
        return dataContainer.let { itemContainerObservableValue ->
            itemContainerObservableValue.value?.let { itemContainer -> itemNameFunc.invoke(itemContainer) }
        }
    }

    open fun onBeforeDispose() {

    }

    open fun onUpdateDataContainer(itemContainer: ItemContainer<T>) {

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
