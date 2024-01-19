package com.fonrouge.fsLib.lib

import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.CrudTask
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import js.uri.encodeURIComponent
import kotlinx.serialization.json.Json

/**
 * Builds a string Url based on a [ConfigViewItem] and a [ApiItem] parameters
 * @return Url string
 */
fun <T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> urlFromApiItem(
    configViewItem: ConfigViewItem<out ICommonContainer<T, ID, FILT>, *, ID, *, *, FILT>,
    apiItem: ApiItem<T, ID, FILT>
): String? {
    val url: String? = when (apiItem.crudTask) {
        CrudTask.Create -> listOf("action" to CrudTask.Create.name)
        else -> {
            apiItem.id?.let { it: ID ->
                listOf(
                    "action" to apiItem.crudTask.name,
                    "id" to Json.encodeToString(configViewItem.commonView.idSerializer, it)
                )
            }
        }
    }?.let { params ->
        val urlParams = UrlParams(*params.toTypedArray())
        apiItem.apiFilter?.let {
            urlParams.pushParam(
                "apiFilter" to encodeURIComponent(
                    Json.encodeToString(
                        configViewItem.commonView.apiFilterSerializer,
                        apiItem.apiFilter
                    )
                )
            )
        }
        configViewItem.url + urlParams.toString()
    }
    return url
}
