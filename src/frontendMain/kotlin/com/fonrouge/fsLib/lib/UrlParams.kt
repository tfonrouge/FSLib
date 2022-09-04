package com.fonrouge.fsLib.lib

import com.fonrouge.fsLib.ContextDataUrl
import com.fonrouge.fsLib.model.CrudAction
import com.fonrouge.fsLib.model.base.BaseModel
import io.kvision.navigo.Match

typealias UrlParam = Pair<String, String>

data class UrlParams(val match: Match? = null) : ArrayList<UrlParam>() {

    constructor(vararg urlParams: UrlParam) : this() {
        addAll(urlParams)
    }

    init {
        val params = match?.params
        if (params != null) {
            for (entry in js("Object").entries(params)) {
                add(Pair(entry[0], entry[1]))
            }
        }
    }

    val crudAction: CrudAction?
        get() {
            return find { it.first == "action" }?.let { pair ->
                CrudAction.values().find { it.name == pair.second }
            }
        }

    val actionUpsert: Boolean
        get() {
            return find { it.first == "action" }?.let {
                it.second in listOf(CrudAction.Create.name, CrudAction.Update.name)
            } ?: false
        }

    val contextDataUrl: ContextDataUrl?
        get() {
            val contextClass = find { it.first == "contextClass" }?.second
            val contextId = find { it.first == "contextId" }?.second
            val contextName = find { it.first == "contextName" }?.second
            return if (contextClass != null && contextId != null) {
                return ContextDataUrl(contextClass, contextId, contextName)
            } else null
        }

    val id: String?
        get() {
            return firstOrNull { it.first == "id" }?.second
        }

    fun addContext(item: BaseModel<*>): UrlParams {
        add("contextClass" to (item.let { item::class.simpleName } ?: ""))
        add("contextId" to JSON.stringify(item._id))
        return this
    }

    override fun toString(): String {
        if (size > 0) {
            var string = ""
            forEach { pair ->
                string += "${if (string == "") "" else "&"}${pair.first}=${pair.second}"
            }
            return "?$string"
        }
        return ""
    }
}
