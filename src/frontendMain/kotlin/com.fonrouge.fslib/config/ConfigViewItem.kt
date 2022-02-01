package com.fonrouge.fslib.config

import com.fonrouge.fslib.apiLib.TypeView
import com.fonrouge.fslib.lib.ActionParam
import com.fonrouge.fslib.lib.UrlParams
import com.fonrouge.fslib.view.ViewItem
import kotlinx.serialization.json.JsonObject

class ConfigViewItem<out V : ViewItem<*, *>>(
    name: String,
    label: String,
    val viewFunc: ((UrlParams?) -> V)? = null,
    val updateData: ((@UnsafeVariance V) -> Unit)? = null,
    val windowModal: Boolean = false,
    restUrl: String? = null,
    restUrlParams: UrlParams? = null,
    lookupParam: JsonObject? = null,
) : BaseConfigView(
    name = name,
    label = label,
    _restUrl = restUrl,
    restUrlParams = restUrlParams,
    lookupParam = lookupParam,
    typeView = TypeView.CItem
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

    val labelInsert = "Agregar $label"
    val labelUpdate = "Modificar $label"
    val labelDelete = "Eliminar $label"
    val labelDetail = "Ver detalle de $label"
}
