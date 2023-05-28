package com.fonrouge.fsLib.lib

import com.fonrouge.fsLib.model.CrudTask
import com.fonrouge.fsLib.model.apiData.ApiList
import com.fonrouge.fsLib.model.base.BaseDoc
import io.kvision.navigo.Match
import js.uri.decodeURIComponent
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import web.buffer.atob
import kotlin.js.Json
import kotlin.js.json

data class UrlParams(
    val match: Match? = null,
    var params: Json = json()
) {

    constructor(vararg urlParams: Pair<String, String>) : this() {
        params = json(*urlParams)
    }

    init {
        match?.let {
            params = match.params.unsafeCast<Json?>() ?: json()
        }
    }

    val crudTask: CrudTask?
        get() {
            return CrudTask.values().find { it.name == params.get("action") }
        }

    val actionUpsert: Boolean
        get() {
            return params.get("action") in listOf(CrudTask.Create.name, CrudTask.Update.name)
        }

    val contextClass: String? get() = params.get("contextClass") as? String
    val contextId: String? get() = params.get("contextId") as? String
    val apiList: ApiList?
        get() {
            val contextClass = params.get("contextClass") as? String
            val contextId = params.get("contextId") as? String
            return if (contextClass != null && contextId != null) {
                return ApiList(contextClass = contextClass, contextId = contextId)
            } else null
        }

    val id: String?
        get() {
            return params.get("id") as? String
        }

    fun addContext(item: BaseDoc<*>?, encodedId: String?): UrlParams {
        params.set("contextClass", item?.let { item::class.simpleName })
        params.set("contextId", encodedId)
        return this
    }

    fun addContext(apiList: ApiList?): UrlParams {
        apiList?.let {
            params.set("contextClass", it.contextClass)
            params.set("contextId", it.contextId)
        }
        return this
    }

    /**
     * Gets an object [T] from the url parameters with the [key] value
     */
    @OptIn(InternalSerializationApi::class)
    @Suppress("unused")
    inline fun <reified T : Any> pullUrlParam(key: String): T? = pullUrlParam(T::class.serializer(), key)

    /**
     * Gets an object [T] from the url parameters with the [key] value
     */
    fun <T : Any> pullUrlParam(serializer: DeserializationStrategy<T>, key: String): T? =
        params[key]?.unsafeCast<String?>()?.let {
            kotlinx.serialization.json.Json.decodeFromString(serializer, atob(decodeURIComponent(it)))
        }

    /**
     * set a para on the params url resulting string
     */
    fun pushParam(param: Pair<String, String>) {
        params[param.first] = param.second
    }

    override fun toString(): String {
        var result = ""
        var size = 0
        for (entry in js("Object").entries(params)) {
            ++size
            result += "${if (result == "") "" else "&"}${entry[0]}=${entry[1]}"
        }
        return if (size > 0) "?$result" else ""
    }
}
