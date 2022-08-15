package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.StateItem
import com.fonrouge.fsLib.apiLib.AppScope
import com.fonrouge.fsLib.apiLib.KVWebManager
import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.lib.KPair
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.CrudAction
import com.fonrouge.fsLib.model.ItemContainer
import com.fonrouge.fsLib.model.base.BaseModel
import io.kvision.core.*
import io.kvision.form.FormPanel
import io.kvision.html.Button
import io.kvision.html.ButtonStyle
import io.kvision.html.button
import io.kvision.i18n.I18n.tr
import io.kvision.panel.flexPanel
import io.kvision.panel.vPanel
import io.kvision.state.ObservableValue
import io.kvision.toast.*
import io.kvision.utils.em
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import org.w3c.dom.events.MouseEvent

@Suppress("unused")
abstract class ViewItem<T : BaseModel<U>, U>(
    override val configView: ConfigViewItem<T, out ViewItem<T, U>, *, U>,
    repeatRefreshView: Boolean? = null,
    editable: Boolean = true,
    icon: String? = null,
    matchFilterParam: JsonObject? = null,
    sortParam: JsonObject? = null,
) : ViewDataContainer<T>(
    configView = configView,
    editable = editable,
    icon = icon,
    restUrlParams = configView.restUrlParams,
    matchFilterParam = matchFilterParam,
    sortParam = sortParam
) {
    open val createDefaultValueList: List<KPair<T, *>>? = null
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
                    itemId = itemId,
                    item = it,
                    contextDataUrl = urlParams?.contextDataUrl
                ) { itemContainer ->
                    dataContainer.value = itemContainer
                }
            }
        }
    }

    private fun displayForm(container: Container, action: CrudAction) {
        container.apply {
            vPanel(className = "showItem") {
                flexPanel(direction = FlexDirection.COLUMN, spacing = 10) {
                    addBeforeDisposeHook {
                        handleInterval = null
                        onBeforeDispose()
                    }
                    if (!noPageBanner) {
                        pageBanner()
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
                            button(tr("Cancel"), style = ButtonStyle.OUTLINEDANGER) {
                                onClick {
                                    js("history.back()") as? Unit
                                }
                            }
                            button(tr("Accept"), style = ButtonStyle.OUTLINESUCCESS) {
//                                marginLeft = 10.px
                                onClick {
                                    if (formPanel?.validate() == true) {
                                        configView.callItemService(
                                            crudAction = action,
                                            callType = StateItem.CallType.Action,
                                            itemId = itemId,
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
                                            message = "Form has incomplete data", options = ToastOptions(
                                                positionClass = ToastPosition.BOTTOMRIGHT
                                            )
                                        )
                                    }
                                }
                            }
                        } else {
                            if (!noBackButton) {
                                button(tr("Back"), icon = "fa-solid fa-arrow-rotate-left").onClick {
                                    js("history.back()") as? Unit
                                }
                            }
                        }
                    }
                }
            }
        }
        when (action) {
            CrudAction.Create -> {
                console.warn("CREATE ASSIGNING")
                dataContainer.value?.item?.let {
                    console.warn("CREATE ASSIGNING it:", it)
                    formPanel?.setData(it)
                }
                createDefaultValueList?.forEach { kPair ->
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

    override fun displayPage(container: Container) {
        when (val action = urlParams?.action) {
            CrudAction.Create, CrudAction.Read, CrudAction.Update -> {
                val params = urlParams?.match?.params
                val _id = if (params == undefined) {
                    null
                } else {
                    params["id"]
                }
                itemId = _id?.unsafeCast<U>()
                configView.callItemService(
                    crudAction = action,
                    callType = StateItem.CallType.Query,
                    itemId = itemId,
                    contextDataUrl = urlParams?.contextDataUrl

                ) { itemContainer ->
                    if (itemContainer.result) {
                        dataContainer.value = itemContainer
                        displayForm(container, action)
                    } else {
                        js("history.back()") as? Unit
                        AppScope.launch {
                            ToastContainer(ToastContainerPosition.MIDDLECENTER)
                                .showToast(
                                    message = itemContainer.description ?: "unknown error...",
                                    title = "!",
                                    bgColor = BsBgColor.DANGER,
                                    color = BsColor.WHITE
                                )
                        }
                    }
                }
            }

            else -> {}
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
        urlParams?.action?.let { crudAction ->
            configView.callItemService(
                crudAction = crudAction,
                callType = StateItem.CallType.Query,
                itemId = itemId,
                contextDataUrl = urlParams?.contextDataUrl
            ) { itemContainer ->
                dataContainer.value = itemContainer
            }
        }
    }
}
