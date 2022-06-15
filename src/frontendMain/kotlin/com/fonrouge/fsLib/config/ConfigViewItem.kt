package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.apiLib.KVWebManager
import com.fonrouge.fsLib.apiLib.TypeView
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.CrudAction
import com.fonrouge.fsLib.model.base.BaseModel
import com.fonrouge.fsLib.view.ViewItem
import kotlinx.serialization.json.JsonObject

abstract class ConfigViewItem<T : BaseModel<*>, V : ViewItem<T, *, *>>(
    name: String,
    label: String,
    viewFunc: ((UrlParams?) -> V),
    val windowModal: Boolean = false,
    restUrlParams: UrlParams? = null,
    lookupParam: JsonObject? = null,
) : ConfigViewContainer<T, V>(
    name = name,
    label = label,
    restUrlParams = restUrlParams,
    lookupParam = lookupParam,
    typeView = TypeView.Item,
    viewFunc = viewFunc,
) {

    val labelDelete = "Eliminar $label"
    val labelDetail = "Ver detalle de $label"
    val labelInsert = "Agregar $label"
    val labelUpdate = "Modificar $label"

    val urlWithInsert: String
        get() {
            val urlParams = UrlParams("action" to CrudAction.Create.name)
            if (windowModal) urlParams.add("window" to "modal")
            return navigoUrl + urlParams.toString()
        }

    fun urlWithDelete(id: Any): String {
        val urlParams = UrlParams("id" to id, "action" to CrudAction.Delete.name)
        if (windowModal) urlParams.add("window" to "modal")
        return navigoUrl + urlParams.toString()
    }

    fun urlWithUpdate(id: Any): String {
        val urlParams = UrlParams("id" to id, "action" to CrudAction.Update.name)
        if (windowModal) urlParams.add("window" to "modal")
        return navigoUrl + urlParams.toString()
    }

    init {
        KVWebManager.configViewItemMap[name] = this
    }
}
