package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.apiLib.KVWebManager
import com.fonrouge.fsLib.apiLib.KVWebManager.pageContainerWidth
import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.layout.centeredMessage
import com.fonrouge.fsLib.lib.ActionParam
import com.fonrouge.fsLib.lib.KPair
import com.fonrouge.fsLib.model.IDataItem
import com.fonrouge.fsLib.model.ItemContainer
import com.fonrouge.fsLib.model.base.BaseModel
import io.kvision.core.Container
import io.kvision.core.FlexDirection
import io.kvision.form.DateFormControl
import io.kvision.form.StringFormControl
import io.kvision.html.Button
import io.kvision.html.ButtonStyle
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.modal.Modal
import io.kvision.panel.flexPanel
import io.kvision.remote.CallAgent
import io.kvision.remote.HttpMethod
import io.kvision.remote.JsonRpcRequest
import io.kvision.remote.KVServiceManager
import io.kvision.state.ObservableValue
import io.kvision.state.bind
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import io.kvision.utils.Serialization
import io.kvision.utils.em
import io.kvision.utils.px
import kotlinx.browser.window
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromDynamic
import kotlinx.serialization.serializer
import org.w3c.dom.events.MouseEvent
import kotlin.js.Date
import kotlin.reflect.KClass

@Suppress("unused")
abstract class ViewItem<T : BaseModel<*>, E : IDataItem>(
    override val configView: ConfigViewItem<T, *>,
    private val serverManager: KVServiceManager<E>,
    private val function: suspend E.(String) -> ItemContainer<T>,
    private val stateFunction: (() -> String?)? = null,
    private val klass: KClass<T>,
    repeatRefreshView: Boolean? = null,
    loading: Boolean = false,
    editable: Boolean = true,
    icon: String? = null,
    matchFilterParam: JsonObject? = null,
    sortParam: JsonObject? = null,
) : ViewDataContainer<T>(
    configView = configView,
    loading = loading,
    editable = editable,
    icon = icon,
    restUrlParams = configView.restUrlParams,
    matchFilterParam = matchFilterParam,
    sortParam = sortParam
) {
    val itemNameFunc: ((ItemContainer<T>) -> String) = { it.item?._id?.toString() ?: "<no item>" }

    override var repeatUpdateView: Boolean? = repeatRefreshView
        get() = field ?: KVWebManager.refreshViewItemPeriodic

    override fun getName(): String? {
        return dataContainer.let {
            itemNameFunc.invoke(it.value)
        }
    }

    var dc2 = ObservableValue<T?>(null)

    var dataContainer: ObservableValue<ItemContainer<T>> = ObservableValue(ItemContainer(null))

    var formPanel: ItemFormPanel<T>? = null
    var origObjItem: dynamic = null

    private var pageContainer: Container? = null

    var onAcceptButtonClick: (Button.(MouseEvent) -> Unit)? = null

    var disableEdit: Boolean = false

    @Suppress("MemberVisibilityCanBePrivate")
    val buttonAccept: Button by lazy {
        Button("Aceptar", style = ButtonStyle.OUTLINESUCCESS)
            .onClick {
                if (formPanel?.validate() == true) {
                    onAcceptButtonClick?.invoke(this, it) ?: upsertItem()
                    (pageContainer as? Modal)?.hide()
                } else {
                    Toast.warning(
                        message = "Datos incompletos",
                        options = ToastOptions(
                            positionClass = ToastPosition.BOTTOMRIGHT
                        )
                    )
                }
            }
    }

    open fun defaultUpsertValueList(item: T?): List<KPair<T, *>> {
        return listOf()
    }

    open fun Container.pageItemBody(item: T?): ItemFormPanel<T>? = null

    fun upsertItem(customUpdate: dynamic = null, block: ((Boolean?) -> Unit)? = null) {
//        KVWebManager.upsertItem(viewItem = this@ViewItem, customUpdate = customUpdate, block = block)
    }

    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    override suspend fun callUpdate() {
        val (url, method) = serverManager.requireCall(function)
        val callAgent = CallAgent()
        val state = stateFunction?.invoke()?.let { JSON.stringify(it) }
        val data = Serialization.plain.encodeToString(JsonRpcRequest(0, url, listOf(state)))
        callAgent.remoteCall(url, data, method = HttpMethod.valueOf(method.name))
            .then { r: dynamic ->
                val result = JSON.parse<dynamic>(r.result.unsafeCast<String>())
                val itemContainer: ItemContainer<T> =
                    Json.decodeFromDynamic(ItemContainer.serializer(klass.serializer()), result)
                dataContainer.value = itemContainer
            }
    }

    final override fun displayPage(container: Container) {

        this.container = container

        val action = urlParams?.action
        if (action == ActionParam.Insert) {
            dataContainer.value = ItemContainer(null)
        }
        pageContainer = container
        container.apply {

/*
            if (container is Modal) {
                container.caption = getCaption()
            } else {
                console.warn("PAGEBANNER 1")
                pageBanner()
                console.warn("PAGEBANNER 2")
            }
*/

            div().bind(dataContainer) { itemContainer ->
                singleRender {
                    container.disposeAll()
                    container.pageBanner()
                    if (action == ActionParam.Update && itemContainer.item == null) {
                        container.centeredMessage("Updating error: item must not be null")
                    } else {
                        container.flexPanel(direction = FlexDirection.COLUMN, spacing = 10, className = "LaCubana") {
                            formPanel = container.pageItemBody(item = itemContainer.item)
                            if (urlParams?.actionUpsert == true) {
                                div(className = "col-$pageContainerWidth-12 text-right") {
                                    marginTop = 1.em
                                    button("Cancelar", style = ButtonStyle.OUTLINEDANGER) {
                                        onClick {
                                            if (container is Modal) {
                                                container.hide()
                                            } else {
/*
                        lastResolved?.let {
                            routing.navigate(it.last().url)
                        } ?: routing.navigate("")
*/
                                            }
                                        }
                                    }
                                    add(buttonAccept)
                                    buttonAccept.marginLeft = 10.px
                                }
                            }
                        }
                        formPanel?.let { formPanel1 ->
                            itemContainer.let { it1 ->
                                it1.item?.let { formPanel1.setData(it) }
                            }
                            formPanel1.form.fields.forEach { formControlEntry ->
                                if (urlParams?.actionUpsert != true || formControlEntry.key == "id" || disableEdit) {
                                    formControlEntry.value.disabled = true
                                }
                                if (urlParams?.action != ActionParam.Insert) {
                                    when (val formControl = formControlEntry.value) {
                                        is DateFormControl -> {
                                            (origObjItem[formControlEntry.key] as? Date)?.let {
                                                formControl.value = it
                                            }
                                        }
                                        is StringFormControl -> {
                                            (origObjItem[formControlEntry.key] as? String)?.let {
                                                formControl.value = it
                                            }
                                        }
                                    }
                                }
                                if (urlParams?.actionUpsert == true) {
                                    defaultUpsertValueList(itemContainer.item).firstOrNull { it.kProp.name == formControlEntry.key }
                                        ?.let {
                                            val value = it.value
                                            if (formControlEntry.value.getValue() == null && value != null) {
                                                formControlEntry.value.setValue(value)
                                            }
                                        }
                                }
                            }
                            /* when more than one selector points to the same data */
                            if (urlParams?.actionUpsert != true || disableEdit) {
                                formPanel1.selectAjaxList.forEach { selCont ->
                                    selCont.select.disabled = true
                                }
                            }
                            val block: () -> Unit = {
                                formPanel1.selectAjaxList.forEach { selCont ->
                                    selCont.select.value = selCont.selectedPair?.first
                                    selCont.select.selectedLabel = selCont.selectedPair?.second
                                }
                            }
                            window.setTimeout(block, 300)
                            var handle: Int? = null
                            if (formPanel1.selectAjaxList.size > 0) {
                                handle = window.setInterval(
                                    handler = block,
                                    timeout = 1000
                                )
                            }
                            formPanel1.addAfterDestroyHook {
                                handle?.let { window.clearInterval(it) }
                            }
                        }
                    }
                }
            }
        }

        if (container is Modal) {
            container.show()
        }

        updateData()
    }
}
