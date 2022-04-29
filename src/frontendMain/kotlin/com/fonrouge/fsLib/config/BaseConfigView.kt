package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.apiLib.TypeView
import com.fonrouge.fsLib.lib.ActionParam
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.base.BaseModel
import com.fonrouge.fsLib.routing.KVAction
import com.fonrouge.fsLib.view.ViewDataContainer
import kotlinx.serialization.json.JsonObject

const val dataUrlPrefix = "data"

private const val navigoPrefix = "#/"

abstract class BaseConfigView<T : BaseModel<*>, V : ViewDataContainer<*>>(
    val name: String,
    val label: String,
    val typeView: TypeView,
    val url: String = "$dataUrlPrefix/$name${typeView.label}",
    val restUrlParams: UrlParams? = null,
    val lookupParam: JsonObject? = null,
    val viewFunc: ((UrlParams?) -> V)? = null,
) : KVAction() {

    val navigoUrl: String = navigoPrefix + url

    fun urlTyped(typeView: TypeView): String {
        return dataUrlPrefix + "/" + name + typeView.label
    }

    fun dispatchViewPage(urlParams: UrlParams?) {
        viewFunc?.invoke(urlParams)?.let { viewDataContainer: V ->
            viewDataContainer.dispatchActionPage()
            if (this !is ConfigViewItem<*, *> && urlParams?.action != ActionParam.Insert) {
                viewDataContainer.updateData()
            }
        }
    }
}
