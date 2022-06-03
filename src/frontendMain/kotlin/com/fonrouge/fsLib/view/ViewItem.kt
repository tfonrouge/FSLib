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
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import io.kvision.utils.Serialization
import io.kvision.utils.em
import io.kvision.utils.obj
import io.kvision.utils.px
import kotlinx.browser.window
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonObject
import org.w3c.dom.events.MouseEvent
import kotlin.js.Date
import kotlin.js.Promise

@Suppress("unused")
abstract class ViewItem<T : BaseModel<*>, E : IDataItem>(
    override val configView: ConfigViewItem<T, *>,
    private val serverManager: KVServiceManager<E>,
    private val function: suspend E.(String) -> ItemContainer<T>,
    private val stateFunction: (() -> String?)? = null,
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

    val itemNameFunc: ((T) -> String) = { it._id.toString() }

    override var repeatRefreshView: Boolean? = repeatRefreshView
        get() = field ?: KVWebManager.refreshViewItemPeriodic

    override fun getName(): String? {
        return dataContainer?.let { itemNameFunc.invoke(it.value) }
    }

    var dataContainer: ObservableValue<T>? = null
        set(value) {
            field = value
            onUpdateDataContainer?.invoke(value?.value)
        }

    var formPanel: ItemFormPanel<T>? = null
    var origObjItem: dynamic = null

    val item: T?
        get() {
            val item = dataContainer
            origObjItem = item?.asDynamic() ?: obj { }
            return item?.value
        }

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

    private fun callServer(): Promise<Any> {
        val (url, method) = serverManager.requireCall(function)
        val callAgent = CallAgent()
        val state = stateFunction?.invoke()?.let { JSON.stringify(it) }
        val data = Serialization.plain.encodeToString(JsonRpcRequest(0, url, listOf(state)))
        return callAgent.remoteCall(url, data, method = HttpMethod.valueOf(method.name))
            .then { r: dynamic ->
                console.warn("callServer r", r)
                val result = JSON.parse<dynamic>(r.result.unsafeCast<String>())
                console.warn("callServer result", result)
            }
    }

    final override fun displayPage(container: Container) {

        this.container = container

        val action = urlParams?.action
        if (action == ActionParam.Insert) {
            dataContainer = null
        }
        callServer()
        pageContainer = container
        container.apply {
            if (container is Modal) {
                container.caption = getCaption()
            } else {
                pageBanner()
            }
            if (loading) {
                centeredMessage("loading...")
            } else {
                if (action == ActionParam.Update && item == null) {
                    centeredMessage("Updating error: item must not be null")
                } else {
                    div(className = "container-$pageContainerWidth show-item") {
                        flexPanel(direction = FlexDirection.COLUMN, spacing = 10) {
                            formPanel = container.pageItemBody(item = item)
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
//                    mediaContainer(this@ViewItem, "default")
                    }
                    formPanel?.let { formPanel1 ->
                        item?.let { it1 ->
                            formPanel1.setData(it1)
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
                                defaultUpsertValueList(item).firstOrNull { it.kProp.name == formControlEntry.key }
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
        if (container is Modal) {
            container.show()
        }
    }

    open fun beforeUpdate(updateData: dynamic) {}
}
