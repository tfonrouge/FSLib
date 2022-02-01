package com.fonrouge.fslib.view

import com.fonrouge.fslib.apiLib.IfceWebAction
import com.fonrouge.fslib.apiLib.KVWebManager
import com.fonrouge.fslib.apiLib.KVWebManager.configViewItemMap
import com.fonrouge.fslib.apiLib.KVWebManager.pageContainerWidth
import com.fonrouge.fslib.config.ConfigViewItem
import com.fonrouge.fslib.layout.centeredMessage
import com.fonrouge.fslib.lib.ActionParam
import com.fonrouge.fslib.lib.KPair
import com.fonrouge.fslib.model.base.BaseContainerItem
import com.fonrouge.fslib.model.base.BaseModel
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
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import io.kvision.utils.em
import io.kvision.utils.px
import kotlinx.browser.window
import kotlinx.serialization.json.JsonObject
import org.w3c.dom.events.MouseEvent
import kotlin.js.Date

@Suppress("unused")
abstract class ViewItem<T : BaseModel, U : BaseContainerItem<T>>(
    name: String,
    val itemNameFunc: ((U) -> String) = { it.item?.id.toString() },
    val configViewItem: ConfigViewItem<ViewItem<*, *>> = configViewItemMap[name]!!,
    repeatRefreshView: Boolean? = null,
    loading: Boolean = false,
    editable: Boolean = true,
    icon: String? = null,
    actionPage: (View) -> IfceWebAction?,
    matchFilterParam: JsonObject? = null,
    sortParam: JsonObject? = null,
) : ViewDataContainer<U>(
    name = name,
    configView = configViewItem,
    loading = loading,
    editable = editable,
    icon = icon,
    actionPage = actionPage,
    restUrlParams = configViewItem.restUrlParams,
    matchFilterParam = matchFilterParam,
    sortParam = sortParam
) {

    override var repeatRefreshView: Boolean? = repeatRefreshView
        get() = field ?: KVWebManager.refreshViewItemPeriodic

    override fun getName(): String? {
        return dataContainer?.let { itemNameFunc.invoke(it) }
    }

    var formPanel: ItemFormPanel<T>? = null
    var origObjItem: dynamic = null

    val item: T?
        get() {
            val item = dataContainer?.item
            origObjItem = item.asDynamic()
            return item
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
                    pullHandleInterval()
/*
                        ?: (lastResolved?.let {
                            routing.navigate(it.last().url)
                        } ?: routing.navigate(""))
*/
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

    abstract fun pageItemBody(container: Container, item: T?): ItemFormPanel<T>?

    fun upsertItem(customUpdate: dynamic = null, block: ((Boolean?) -> Unit)? = null) {
        KVWebManager.upsertItem(viewItem = this@ViewItem, customUpdate = customUpdate, block = block)
    }

    final override fun displayPage(container: Container) {

        this.container = container

        val action = urlParams?.action
        if (action == ActionParam.Insert) {
            dataContainer = null
        }
        pageContainer = container
        container.apply {
            if (container is Modal) {
                container.caption = getCaption()
                pushHandleInterval()
            } else {
                pageBanner(this@ViewItem)
            }
            if (loading) {
                centeredMessage("loading...")
            } else {
                if (action == ActionParam.Update && item == null) {
                    centeredMessage("Updating error: item must not be null")
                } else {
                    div(className = "container-$pageContainerWidth show-item") {
                        flexPanel(direction = FlexDirection.COLUMN, spacing = 10) {
                            formPanel = pageItemBody(container = this, item = item)
                            if (urlParams?.actionUpsert == true) {
                                div(className = "col-$pageContainerWidth-12 text-right") {
                                    marginTop = 1.em
                                    button("Cancelar", style = ButtonStyle.OUTLINEDANGER) {
                                        onClick {
                                            if (container is Modal) {
                                                container.hide()
                                                pullHandleInterval()
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
                            when (val formControl = formControlEntry.value) {
                                is DateFormControl -> {
                                    // TODO: fix deserializing date with default const KV_DEFAULT_DATE_FORMAT in String.toDateF()
                                    formControl.value = origObjItem?._get(formControlEntry.key) as? Date
                                }
                                is StringFormControl -> {
                                    formControl.value = origObjItem?._get(formControlEntry.key) as? String
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
}
