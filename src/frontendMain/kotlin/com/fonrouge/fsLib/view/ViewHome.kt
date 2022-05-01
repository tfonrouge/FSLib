package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.config.ConfigViewHome
import com.fonrouge.fsLib.lib.UrlParams
import kotlinx.serialization.json.JsonObject

abstract class ViewHome(
    configView: ConfigViewHome<*>,
    loading: Boolean = false,
    editable: Boolean = true,
    icon: String? = null,
    restUrlParams: UrlParams? = null,
    matchFilterParam: JsonObject? = null,
    sortParam: JsonObject? = null,
    upsertData: JsonObject? = null,
    modal: Boolean = false
) : View(
    configView = configView,
    loading = loading,
    editable = editable,
    icon = icon,
    restUrlParams = restUrlParams,
    matchFilterParam = matchFilterParam,
    sortParam = sortParam,
    upsertData = upsertData,
    modal = modal
)
