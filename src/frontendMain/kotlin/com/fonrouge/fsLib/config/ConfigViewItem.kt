package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.apiLib.KVWebManager
import com.fonrouge.fsLib.apiLib.TypeView
import com.fonrouge.fsLib.lib.ActionParam
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.base.BaseContainerItem
import com.fonrouge.fsLib.model.base.BaseModel
import com.fonrouge.fsLib.view.ViewItem
import kotlinx.serialization.json.JsonObject
import kotlin.reflect.KSuspendFunction1

class ConfigViewItem<T : BaseModel<*>, U : BaseContainerItem<T>, V : ViewItem<T, U>>(
    name: String,
    label: String,
    viewFunc: ((UrlParams?) -> V)? = null,
    dataFunc: KSuspendFunction1<V, U?>,
    val windowModal: Boolean = false,
    restUrl: String? = null,
    restUrlParams: UrlParams? = null,
    lookupParam: JsonObject? = null,
) : BaseConfigView<U, V>(
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

    val labelInsert = "Agregar $label"
    val labelUpdate = "Modificar $label"
    val labelDelete = "Eliminar $label"
    val labelDetail = "Ver detalle de $label"

    init {
        KVWebManager.configViewItemMap[name] = this
    }
}
