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

    fun labelUrlRead(id: Any) = label to urlRead(id)

    val urlCreate: String
        get() {
            val urlParams = UrlParams("action" to CrudAction.Create.name)
            return navigoUrl + urlParams.toString()
        }

    fun urlRead(id: Any): String {
        val urlParams = UrlParams("id" to id, "action" to CrudAction.Read.name)
        return navigoUrl + urlParams.toString()
    }

    fun urlDelete(id: Any): String {
        val urlParams = UrlParams("id" to id, "action" to CrudAction.Delete.name)
        return navigoUrl + urlParams.toString()
    }

    fun urlUpdate(id: Any): String {
        val urlParams = UrlParams("id" to id, "action" to CrudAction.Update.name)
        return navigoUrl + urlParams.toString()
    }

    init {
        KVWebManager.configViewItemMap[name] = this
    }
}
