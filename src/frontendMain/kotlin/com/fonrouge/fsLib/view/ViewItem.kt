package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.apiLib.KVWebManager
import com.fonrouge.fsLib.apiLib.KVWebManager.pageContainerWidth
import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.lib.ActionParam
import com.fonrouge.fsLib.lib.KPair
import com.fonrouge.fsLib.model.IDataItem
import com.fonrouge.fsLib.model.ItemContainer
import com.fonrouge.fsLib.model.base.BaseModel
import io.kvision.core.Container
import io.kvision.core.FlexDirection
import io.kvision.form.FormPanel
import io.kvision.html.Button
import io.kvision.html.ButtonStyle
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.i18n.I18n.tr
import io.kvision.panel.flexPanel
import io.kvision.panel.vPanel
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
import io.kvision.utils.px
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
    private val function: suspend E.(U, String?) -> ItemContainer<T>,
    private val stateFunction: (() -> String?)? = null,
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
    var dataContainer: ObservableValue<ItemContainer<T>> = ObservableValue(ItemContainer(null))
    var disableEdit: Boolean = false
    var formPanel: FormPanel<T>? = null
    val item get() = dataContainer.value.item
    var itemId: U? = null
    val itemNameFunc: ((ItemContainer<T>) -> String) = { it.item?._id?.toString() ?: "<no item>" }
    var onAcceptButtonClick: (Button.(MouseEvent) -> Unit)? = null

    //    var origObjItem: dynamic = null
    private var pageContainer: Container? = null
    override var repeatUpdateView: Boolean? = repeatRefreshView
        get() = field ?: KVWebManager.refreshViewItemPeriodic

    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    override suspend fun callUpdate() {
        val (url, method) = serverManager.requireCall(function)
        val callAgent = CallAgent()
        val state = stateFunction?.invoke()?.let { JSON.stringify(it) }
        val data = Serialization.plain.encodeToString(JsonRpcRequest(0, url, listOf(JSON.stringify(itemId), state)))
        console.warn("callUpdate data", data)
        callAgent.remoteCall(url, data, method = HttpMethod.valueOf(method.name))
            .then { r: dynamic ->
                val result = JSON.parse<dynamic>(r.result.unsafeCast<String>())
                val itemContainer: ItemContainer<T> =
                    Json.decodeFromDynamic(ItemContainer.serializer(klass.serializer()), result)
                dataContainer.value = itemContainer
            }
    }

    open fun defaultUpsertValueList(item: T?): List<KPair<T, *>> {
        return listOf()
    }

    final override fun displayPage(container: Container) {

        this.container = container

        val action = urlParams?.action
        if (action == ActionParam.Insert) {
            dataContainer.value = ItemContainer(null)
        } else {
            val _id = urlParams?.match?.params["id"] as? String
            console.warn("_id", _id)
            itemId = _id.unsafeCast<U>()
            console.warn("ITEM ID", itemId)
        }
        pageContainer = container
        container.apply {
            vPanel(className = "showItem") {
                addBeforeDisposeHook {
                    handleInterval = null
                    onBeforeDispose()
                }
                pageBanner()
                flexPanel(direction = FlexDirection.COLUMN, spacing = 10) {
                    formPanel = pageItemBody()
                    if (urlParams?.actionUpsert == true) {
                        div(className = "col-$pageContainerWidth-12 text-right") {
                            marginTop = 1.em
                            button(tr("Cancel"), style = ButtonStyle.OUTLINEDANGER) {
                                onClick {
                                    js("history.back()") as Unit
                                }
                            }
                            button(tr("Accept"), style = ButtonStyle.OUTLINESUCCESS) {
                                onClick {
                                    if (formPanel?.validate() == true) {
                                        js("history.back()") as Unit
                                    } else {
                                        Toast.warning(
                                            message = "Datos incompletos",
                                            options = ToastOptions(
                                                positionClass = ToastPosition.BOTTOMRIGHT
                                            )
                                        )
                                    }
                                }
                                marginLeft = 10.px
                            }
                        }
                    }
                }
            }
        }

        dataContainer.subscribe { itemContainer ->
            itemContainer.item?.let {
                console.warn("DEBUG: getting itemContainer.item", it, formPanel)
                formPanel?.setData(it)
//                formPanel?.form?.fields?.forEach {
//                    it.value.setValue("Juana La Cubana")
//                }
            }
        }

        if (action != ActionParam.Insert) {
            updateData()
        }
    }

    override fun getName(): String? {
        return dataContainer.let {
            itemNameFunc.invoke(it.value)
        }
    }

    open fun onBeforeDispose() {

    }

    open fun onUpdateDataContainer(itemContainer: ItemContainer<T>) {

    }

    open fun Container.pageItemBody(): FormPanel<T>? = null
}
