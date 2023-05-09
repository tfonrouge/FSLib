package com.fonrouge.fsLib.lib

import com.fonrouge.fsLib.model.CrudTask
import com.fonrouge.fsLib.model.apiData.ApiList
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

    val crudTask: CrudTask?
        get() {
            return CrudTask.values().find { it.name == params?.get("action") }
        }

    val actionUpsert: Boolean
        get() {
            return params?.get("action") in listOf(CrudTask.Create.name, CrudTask.Update.name)
        }

    val contextClass: String? get() = params?.get("contextClass") as? String
    val contextId: String? get() = params?.get("contextId") as? String
    val apiList: ApiList?
        get() {
            val contextClass = params?.get("contextClass") as? String
            val contextId = params?.get("contextId") as? String
            return if (contextClass != null && contextId != null) {
                return ApiList(contextClass = contextClass, contextId = contextId)
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

    fun addContext(apiList: ApiList?): UrlParams {
        if (params == null) params = json()
        apiList?.let {
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
