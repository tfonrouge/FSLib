package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.ApiParam
import com.fonrouge.fsLib.apiLib.KVWebManager
import com.fonrouge.fsLib.apiLib.TypeView
import com.fonrouge.fsLib.lib.ActionParam
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.base.BaseModel
import com.fonrouge.fsLib.view.ViewItem
import io.kvision.modal.ModalSize
import kotlinx.serialization.json.JsonObject
import kotlin.reflect.KSuspendFunction1

open class ConfigViewItem<T : BaseModel<*>, V : ViewItem<T>>(
    name: String,
    label: String,
    viewFunc: ((UrlParams?) -> V)? = null,
    dataFunc: KSuspendFunction1<ApiParam, T?>,
    val windowModal: Boolean = false,
    restUrl: String? = null,
    restUrlParams: UrlParams? = null,
    lookupParam: JsonObject? = null,
) : BaseConfigView<T, V>(
    name = name,
    label = label,
    _restUrl = restUrl,
    restUrlParams = restUrlParams,
    lookupParam = lookupParam,
    typeView = TypeView.CItem,
    viewFunc = viewFunc,
    dataFunc = dataFunc,
) {

    val urlWithInsert: String
        get() {
            val urlParams = UrlParams("action" to ActionParam.Insert.name)
            if (windowModal) urlParams.add("window" to "modal")
            return navigoUrl + urlParams.toString()
        }

    fun urlWithUpdate(id: Any): String {
        val urlParams = UrlParams("id" to id, "action" to ActionParam.Update.name)
        if (windowModal) urlParams.add("window" to "modal")
        return navigoUrl + urlParams.toString()
    }

    fun urlWithDelete(id: Any): String {
        val urlParams = UrlParams("id" to id, "action" to ActionParam.Delete.name)
        if (windowModal) urlParams.add("window" to "modal")
        return navigoUrl + urlParams.toString()
    }

    fun displayModal(urlParams: UrlParams?, block: (V.() -> Unit)?) {
        viewFunc?.invoke(urlParams)?.let { viewItem ->
            block?.let { block.invoke(viewItem) }
            viewItem.displayBlock = {
                viewItem.displayModal(
                    caption = "Updating this...",
                    size = ModalSize.XLARGE,
                    centered = true
                )
            }
            viewItem.updateData()
        }
    }

    val labelInsert = "Agregar $label"
    val labelUpdate = "Modificar $label"
    val labelDelete = "Eliminar $label"
    val labelDetail = "Ver detalle de $label"

    init {
        KVWebManager.configViewItemMap[name] = this
    }
}
