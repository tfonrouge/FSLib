package com.fonrouge.fslib.lib

import com.fonrouge.fslib.model.base.BaseModel
import io.kvision.navigo.Match

typealias UrlParam = Pair<String, *>

class UrlParams(match: Match? = null) : ArrayList<UrlParam>() {

    constructor(vararg urlParams: UrlParam) : this() {
        addAll(urlParams)
    }

    constructor(item: BaseModel) : this() {
        add("id" to item.id)
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

    val contextPair: Pair<String, String>?
        get() {
            val first = find { it.first == "contextClass" }?.second
            val second = find { it.first == "contextId" }?.second as String?
            return if (first != null && first is String && second != null) {
                return first to second
            } else null
        }

    fun key(key: String): Any? {
        return find { it.first == key }?.second
    }

    val pairClassId: Pair<String, Any>?
        get() {
            val first = find { it.first == "class" }?.second
            val second = find { it.first == "id" }?.second
            return if (first != null && first is String && second != null) {
                return first to second
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

enum class ActionParam {
    Insert,
    Update,
    Delete,
}
