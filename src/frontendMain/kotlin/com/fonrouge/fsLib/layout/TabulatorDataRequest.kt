/*
 * Copyright (c) 2017-present Robert Jaros
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.fonrouge.fsLib.layout

import com.fonrouge.fsLib.model.RequestDataList
import io.kvision.core.Container
import io.kvision.remote.CallAgent
import io.kvision.remote.HttpMethod
import io.kvision.remote.JsonRpcRequest
import io.kvision.remote.KVServiceMgr
import io.kvision.remote.RemoteFilter
import io.kvision.remote.RemoteSerialization
import io.kvision.remote.RemoteSorter
import io.kvision.tabulator.TableType
import io.kvision.tabulator.Tabulator
import io.kvision.tabulator.TabulatorOptions
import io.kvision.utils.Serialization
import kotlinx.browser.window
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromDynamic
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.overwriteWith
import org.w3c.dom.get
import org.w3c.fetch.RequestInit
import kotlin.js.JSON
import kotlin.reflect.KClass

/**
 * Tabulator component connected to the fullstack service.
 *
 * @constructor
 * @param T type of row data
 * @param E type of service manager
 * @param serviceManager fullstack service manager
 * @param function fullstack service method returning tabulator rows data
 * @param stateFunction a function to generate the state object passed with the remote request
 * @param options tabulator options
 * @param types a set of table types
 * @param className CSS class names
 * @param kClass Kotlin class
 * @param serializer the serializer for type T
 * @param module optional serialization module with custom serializers
 * @param requestFilter a request filtering function
 */
open class TabulatorDataRequest<T : Any, E : Any>(
    private val serviceManager: KVServiceMgr<E>,
    private val function: suspend E.(Int?, Int?, List<RemoteFilter>?, List<RemoteSorter>?, String?, Long?) -> RequestDataList<T>,
    private val stateFunction: (() -> String)? = null,
    options: TabulatorOptions<T> = TabulatorOptions(),
    types: Set<TableType> = setOf(),
    className: String? = null,
    kClass: KClass<T>? = null,
    serializer: KSerializer<T>? = null,
    module: SerializersModule? = null,
    private val requestFilter: (suspend RequestInit.() -> Unit)? = null
) : Tabulator<T>(listOf(), false, options, types, className, kClass, serializer, module) {

    var checksum: Long? = null

    override val jsonHelper = if (serializer != null) Json(
        from = (RemoteSerialization.customConfiguration ?: Serialization.customConfiguration ?: Json {
            ignoreUnknownKeys = true
            isLenient = true
        })
    ) {
        serializersModule = SerializersModule {
            include(RemoteSerialization.plain.serializersModule)
            module?.let { this.include(it) }
        }.overwriteWith(serializersModule)
    } else null

    private val kvUrlPrefix = window["kv_remote_url_prefix"]
    private val urlPrefix: String = if (kvUrlPrefix != undefined) "$kvUrlPrefix/" else ""

    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    fun dataRequest() {
        val (url, method) = serviceManager.requireCall(function)
        val callAgent = CallAgent()
        val page = if (jsTabulator?.getPage() != null) "" + jsTabulator?.getPage() else null
        val size = "" + jsTabulator?.getPageSize()
        val filters = JSON.stringify(jsTabulator?.getFilters(true))
        console.warn("getSorters()", jsTabulator)
//        options.columns?.forEach {columnDefinition ->
//            console.warn("sorterParams", columnDefinition, columnDefinition.sorterParams)
//        }
        val sorters = JSON.stringify(js("[]")) //JSON.stringify(jsTabulator?.getSorters())
        val state = stateFunction?.invoke()?.let { JSON.stringify(it) }
        val params = listOf(page, size, filters, sorters, state, Json.encodeToString(checksum))
        val data =
            Serialization.plain.encodeToString(
                JsonRpcRequest(
                    0,
                    url,
                    params
                )
            )

        console.warn("on dataRequest()", urlPrefix, url, params)

        callAgent.remoteCall(
            url,
            data,
            method = HttpMethod.valueOf(method.name),
            requestFilter = requestFilter
        ).then { r: dynamic ->
            val result = JSON.parse<dynamic>(r.result.unsafeCast<String>())
            val response = if (page != null) {
                if (result.data == undefined) {
                    result.data = js("[]")
                }
                result
            } else if (result.data == undefined) {
                js("[]")
            } else {
                result.data
            }
            checksum = Json.decodeFromDynamic<Long?>(response.checksum)
            val changedList: Boolean = Json.decodeFromDynamic(response.changedList)
            console.warn("response", response)
            jsTabulator?.setMaxPage(response.last_page)
            if (changedList) {
                kClass?.serializer()?.let { kSerializer ->
                    val list = Json.decodeFromString(ListSerializer(kSerializer), JSON.stringify(response.data))
                    console.warn("LIST =", list)
                    setData(list.toTypedArray())
                }
            }
        }
    }

    /*
        init {
            val (url, method) = serviceManager.requireCall(function)

            val callAgent = CallAgent()

            options.ajaxURL = urlPrefix + url.drop(1)
            options.ajaxRequestFunc = { _, _, params ->
                @Suppress("UnsafeCastFromDynamic")
                val page = if (params.page != null) "" + params.page else null

                @Suppress("UnsafeCastFromDynamic")
                val size = if (params.size != null) "" + params.size else null

                @Suppress("UnsafeCastFromDynamic")
                val filters = if (params.filter != null) {
                    JSON.stringify(params.filter)
                } else {
                    null
                }

                @Suppress("UnsafeCastFromDynamic")
                val sorters = if (params.sort != null) {
                    JSON.stringify(params.sort)
                } else {
                    null
                }
                val state = stateFunction?.invoke()?.let { JSON.stringify(it) }

                @Suppress("UnsafeCastFromDynamic")
                val data =
                    Serialization.plain.encodeToString(JsonRpcRequest(0, url, listOf(page, size, filters, sorters, state)))
                callAgent.remoteCall(url, data, method = HttpMethod.valueOf(method.name), requestFilter = requestFilter)
                    .then { r: dynamic ->
                        val result = JSON.parse<dynamic>(r.result.unsafeCast<String>())
                        @Suppress("UnsafeCastFromDynamic")
                        if (page != null) {
                            if (result.data == undefined) {
                                result.data = js("[]")
                            }
                            result
                        } else if (result.data == undefined) {
                            js("[]")
                        } else {
                            result.data
                        }
                    }
            }
        }
    */
}

/**
 * DSL builder extension function.
 *
 * It takes the same parameters as the constructor of the built component.
 */
inline fun <reified T : Any, E : Any> Container.tabulatorDataRequest(
    serviceManager: KVServiceMgr<E>,
    noinline function: suspend E.(Int?, Int?, List<RemoteFilter>?, List<RemoteSorter>?, String?, Long?) -> RequestDataList<T>,
    noinline stateFunction: (() -> String)? = null,
    options: TabulatorOptions<T> = TabulatorOptions(),
    types: Set<TableType> = setOf(),
    className: String? = null,
    serializer: KSerializer<T>? = null,
    module: SerializersModule? = null,
    noinline requestFilter: (suspend RequestInit.() -> Unit)? = null,
    noinline init: (TabulatorDataRequest<T, E>.() -> Unit)? = null
): TabulatorDataRequest<T, E> {
    val tabulatorDataRequest =
        TabulatorDataRequest(
            serviceManager,
            function,
            stateFunction,
            options,
            types,
            className,
            T::class,
            serializer,
            module,
            requestFilter
        )
    init?.invoke(tabulatorDataRequest)
    this.add(tabulatorDataRequest)
    return tabulatorDataRequest
}
