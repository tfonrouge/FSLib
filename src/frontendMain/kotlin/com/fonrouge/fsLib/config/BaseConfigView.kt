package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.apiLib.Api
import com.fonrouge.fsLib.apiLib.KVWebManager
import com.fonrouge.fsLib.apiLib.TypeView
import com.fonrouge.fsLib.lib.UrlParams
import io.kvision.form.select.AjaxOptions
import io.kvision.form.select.DataType
import io.kvision.form.select.HttpType
import io.kvision.utils.obj
import kotlinx.serialization.json.JsonObject
import kotlin.reflect.KProperty1

const val dataUrlPrefix = "data"

private const val navigoPrefix = "#/"

open class BaseConfigView(
    val name: String,
    val label: String,
    val typeView: TypeView,
    val url: String = "$dataUrlPrefix/$name${typeView.label}",
    private val _restUrl: String? = null,
    val restUrlParams: UrlParams? = null,
    val lookupParam: JsonObject? = null,
) {
    val navigoUrl: String = navigoPrefix + url
    val restUrl = Api.API_BASE_URL + (_restUrl?.let { dataUrlPrefix + "/" + it + typeView.label } ?: url)

    fun urlTyped(typeView: TypeView): String {
        return dataUrlPrefix + "/" + name + typeView.label
    }

    fun restUrlCustom(map: String): String {
        return Api.API_BASE_URL + (_restUrl?.let { "$dataUrlPrefix/$it/$map" }
            ?: ("$dataUrlPrefix/$name/$map"))
    }

    fun restUrlTyped(typeView: TypeView): String {
        return Api.API_BASE_URL + (_restUrl?.let { dataUrlPrefix + "/" + it + typeView.label } ?: urlTyped(typeView))
    }

    fun <T> ajaxOptions(kProperty1: KProperty1<T, *>? = null): AjaxOptions {
        return AjaxOptions(
            url = restUrlTyped(TypeView.SelectList),
            beforeSend = KVWebManager::authRequest,
            httpType = HttpType.POST,
            dataType = DataType.JSON,
            data = obj {
                q = "{{{q}}}"
                fieldName = kProperty1?.name
            },
            minLength = 1,
            requestDelay = 1000
        )
    }
}
