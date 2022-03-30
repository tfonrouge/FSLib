package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.AppScope
import com.fonrouge.fsLib.apiLib.*
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.base.BaseContainer
import com.fonrouge.fsLib.view.ViewDataContainer
import io.kvision.form.select.AjaxOptions
import io.kvision.form.select.DataType
import io.kvision.form.select.HttpType
import io.kvision.utils.obj
import kotlinx.browser.window
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlin.js.Date
import kotlin.reflect.KProperty1
import kotlin.reflect.KSuspendFunction1

const val dataUrlPrefix = "data"

private const val navigoPrefix = "#/"

open class BaseConfigView<U : BaseContainer, V : ViewDataContainer<U>>(
    val name: String,
    val label: String,
    val typeView: TypeView,
    val url: String = "$dataUrlPrefix/$name${typeView.label}",
    private val _restUrl: String? = null,
    val restUrlParams: UrlParams? = null,
    val lookupParam: JsonObject? = null,
    val viewFunc: ((UrlParams?) -> V)? = null,
    val dataFunc: KSuspendFunction1<V, U?>,
) : KVAction() {

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

    fun updateData(urlParams: UrlParams?) {
        viewFunc?.invoke(urlParams)?.let { view ->
            var loading = if (!view.skipLoading) {
                KVWebManager.kvWebStore.dispatch(IfceWebAction.Loading(view))
                true
            } else {
                view.skipLoading = false
                false
            }
            val callBlock: () -> Unit = {
                try {
                    AppScope.launch {
                        console.warn("before dataFunc()")
                        dataFunc(view).let {
                            console.warn("after dataFunc()", it)
                            view.dataContainer = it
                            if (loading) {
                                loading = false
                                KVWebManager.kvWebStore.dispatch(IfceWebAction.Loaded(view))
                            }
//                            block?.invoke(it)
                            view.displayBlock?.let { it() }
                        }
                    }
                } catch (e: Exception) {
                    console.warn("Error on interval =", e)
                }
            }
            if (view.repeatRefreshView == true) {
                var lastTime: Int? = null
                var lock = false
                ViewDataContainer.handleInterval = window.setInterval(
                    handler = {
                        val time = Date().getUTCSeconds()
                        if (lastTime != Date().getSeconds() && (time % KVWebManager.intervalTimeout == 0)) {
                            lastTime = Date().getUTCSeconds()
                            if (!lock) {
                                lock = true
                                callBlock()
                                lock = false
                            }
                        }
                    },
                    timeout = 250
                )
            }
            callBlock()
        }
    }
}
