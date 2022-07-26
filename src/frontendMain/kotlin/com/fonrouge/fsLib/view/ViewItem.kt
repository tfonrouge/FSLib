package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.StateItem
import com.fonrouge.fsLib.apiLib.AppScope
import com.fonrouge.fsLib.apiLib.KVWebManager
import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.lib.KPair
import com.fonrouge.fsLib.model.CrudAction
import com.fonrouge.fsLib.model.IDataItem
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
import io.kvision.remote.CallAgent
import io.kvision.remote.HttpMethod
import io.kvision.remote.JsonRpcRequest
import io.kvision.remote.KVServiceManager
import io.kvision.state.ObservableValue
import io.kvision.toast.*
import io.kvision.utils.Serialization
import io.kvision.utils.em
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromDynamic
import kotlinx.serialization.serializer
import org.w3c.dom.events.MouseEvent
import kotlin.reflect.KClass

@Suppress("unused")
abstract class ViewItem<T : BaseModel<U>, E : IDataItem, U>(
    override val configView: ConfigViewItem<T, *>,
    private val serverManager: KVServiceManager<E>,
    private val function: suspend E.(U?, StateItem<T>) -> ItemContainer<T>,
    private val stateFunction: (() -> String)? = null,
    private val klass: KClass<T>,
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
    var dataContainer: ObservableValue<ItemContainer<T>?> = ObservableValue(null)
    var disableEdit: Boolean = false
    var formPanel: FormPanel<T>? = null
    val item get() = dataContainer.value?.item
    var itemId: U? = null
    val itemNameFunc: ((ItemContainer<T>) -> String) = { it.item?._id?.toString() ?: "<no item>" }
    var noBackButton = false
    var noPageBanner = false
    var onAcceptButtonClick: (Button.(MouseEvent) -> Unit)? = null
    override var repeatUpdateView: Boolean? = repeatRefreshView
        get() = field ?: KVWebManager.refreshViewItemPeriodic

    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    fun getItemContainer(
        crudAction: CrudAction,
        itemId: U?,
        item: T?,
        callType: StateItem.CallType,
        block: (ItemContainer<T>) -> Unit,
    ) {
        val (url, method) = serverManager.requireCall(function)
        val callAgent = CallAgent()
        val paramList = listOf(
            JSON.stringify(itemId),
            Json.encodeToString(
                serializer = StateItem.serializer(klass.serializer()),
                value = StateItem(
                    item = item,
                    json = null,
                    crudAction = crudAction,
                    callType = callType,
                    state = stateFunction?.invoke()
                )
            )
        )
        val data = Serialization.plain.encodeToString(
            JsonRpcRequest(
                id = 0,
                method = url,
                params = paramList
            )
        )
        callAgent.remoteCall(url, data, method = HttpMethod.valueOf(method.name)).then { r: dynamic ->
            val result = JSON.parse<dynamic>(r.result.unsafeCast<String>())
            val itemContainer: ItemContainer<T> =
                Json.decodeFromDynamic(ItemContainer.serializer(klass.serializer()), result)
            block(itemContainer)
        }
    }

    override suspend fun singleUpdate() {
        urlParams?.action?.let { crudAction ->
            getItemContainer(
                crudAction = crudAction,
                itemId = itemId,
                item = null,
                callType = StateItem.CallType.Query
            ) { itemContainer ->
                dataContainer.value = itemContainer
            }
        }
    }

    open val createDefaultValueList: List<KPair<T, *>>? = null

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
                                        getItemContainer(
                                            crudAction = action,
                                            itemId = itemId,
                                            item = formPanel?.getData(),
                                            callType = StateItem.CallType.Action,
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
                createDefaultValueList?.forEach { kPair ->
                    formPanel?.form?.fields?.asIterable()
                        ?.firstOrNull { kPair.kProp.name == it.key }?.value?.setValue(
                            kPair.value
                        )
                }
            }
            CrudAction.Read -> {
                dataContainer.value?.item?.let { formPanel?.setData(it) }
                dataContainer.subscribe {
                    it?.item?.let { item ->
                        linkBanner?.label = getCaption()
                        formPanel?.setData(item)
                    }
                }
                updateData(false)
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
                getItemContainer(
                    crudAction = action,
                    itemId = itemId,
                    item = null,
                    callType = StateItem.CallType.Query
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

    open fun Container.pageItemBody(): FormPanel<T>? = null
}
