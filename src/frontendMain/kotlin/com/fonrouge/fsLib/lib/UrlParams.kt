package com.fonrouge.fsLib.lib

import com.fonrouge.fsLib.model.base.BaseModel
import io.kvision.navigo.Match

typealias UrlParam = Pair<String, *>

class UrlParams(val match: Match? = null) : ArrayList<UrlParam>() {

    constructor(vararg urlParams: UrlParam) : this() {
        addAll(urlParams)
    }

    constructor(item: BaseModel<*>) : this() {
        add("id" to item._id)
        add("class" to item::class.simpleName)
    }

    init {
        val params = match?.params
        if (params != null) {
            for (entry in js("Object").entries(params)) {
                add(Pair(entry[0], entry[1]))
            }
        }
    }

    val action: ActionParam?
        get() {
            return find { it.first == "action" }?.let { pair ->
                ActionParam.values().find { it.name == pair.second }
            }
        }

    val actionUpsert: Boolean
        get() {
            return find { it.first == "action" }?.let {
                it.second in listOf(ActionParam.Insert.name, ActionParam.Update.name)
            } ?: false
        }

    val contextClassId: ContextClassId?
        get() {
            val contextClass = find { it.first == "contextClass" }?.second
            val contextId = find { it.first == "contextId" }?.second as? String
            val contextName = find { it.first == "contextName" }?.second as? String
            return if (contextClass != null && contextClass is String && contextId != null) {
                return ContextClassId(contextClass, contextId, contextName)
            } else null
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

class ContextClassId(
    val contextClass: String,
    val contextId: String,
    val contextName: String?,
)

enum class ActionParam {
    Insert,
    Update,
    Delete,
}
