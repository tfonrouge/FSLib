package com.fonrouge.fsLib.lib

import com.fonrouge.fsLib.ContextDataUrl
import com.fonrouge.fsLib.model.CrudAction
import com.fonrouge.fsLib.model.base.BaseDoc
import io.kvision.navigo.Match
import kotlin.js.Json
import kotlin.js.json

data class UrlParams(
    val match: Match? = null,
    var params: Json? = null
) : ArrayList<Pair<String, String>>() {

    constructor(vararg urlParams: Pair<String, String>) : this() {
        params = json(*urlParams)
    }

    init {
        match?.let { params = match.params.unsafeCast<Json?>() }
    }

    val crudAction: CrudAction?
        get() {
            return CrudAction.values().find { it.name == params?.get("action") }
        }

    val actionUpsert: Boolean
        get() {
            return params?.get("action") in listOf(CrudAction.Create.name, CrudAction.Update.name)
        }

    val contextDataUrl: ContextDataUrl?
        get() {
            val contextClass = params?.get("contextClass") as? String
            val contextId = params?.get("contextId") as? String
            return if (contextClass != null && contextId != null) {
                return ContextDataUrl(contextClass = contextClass, contextId = contextId)
            } else null
        }

    val id: String?
        get() {
            return params?.get("id") as? String
        }

    fun addContext(item: BaseDoc<*>?, encodedId: String?): UrlParams {
        if (params == null) params = json()
        params?.set("contextClass", item?.let { item::class.simpleName })
        params?.set("contextId", encodedId)
        return this
    }

    fun addContext(contextDataUrl1: ContextDataUrl?): UrlParams {
        if (params == null) params = json()
        contextDataUrl1?.let {
            params?.set("contextClass", it.contextClass)
            params?.set("contextId", it.contextId)
        }
        return this
    }

    override fun toString(): String {
        var result = ""
        var size = 0
        params?.let {
            for (entry in js("Object").entries(it)) {
                ++size
                result += "${if (result == "") "" else "&"}${entry[0]}=${entry[1]}"
            }
        }
        return if (size > 0) "?$result" else ""
    }
}
