package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.apiLib.KVWebManager
import com.fonrouge.fsLib.apiLib.TypeView
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.base.BaseContainerList
import com.fonrouge.fsLib.model.base.BaseModel
import com.fonrouge.fsLib.view.ViewList
import kotlinx.serialization.json.JsonObject
import kotlin.reflect.KSuspendFunction1

open class ConfigViewList<T : BaseModel<*>, U : BaseContainerList<T>, V : ViewList<T, U>>(
    name: String,
    label: String,
    viewFunc: ((UrlParams?) -> V)? = null,
    dataFunc: KSuspendFunction1<V, U?>,
    restUrl: String? = null,
    restUrlParams: UrlParams? = null,
    lookupParam: JsonObject? = null,
) : BaseConfigView<U, V>(
    name = name,
    label = label,
    _restUrl = restUrl,
    restUrlParams = restUrlParams,
    lookupParam = lookupParam,
    typeView = TypeView.CList,
    viewFunc = viewFunc,
    dataFunc = dataFunc,
) {

    init {
        KVWebManager.configViewListMap[name] = this
    }
}
