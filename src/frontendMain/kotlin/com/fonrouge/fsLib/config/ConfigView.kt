package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.view.View
import js.uri.encodeURIComponent
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import web.buffer.btoa
import kotlin.reflect.KClass

private const val navigoPrefix = "#/"

/*
    TODO: encode/decode baseUrl to be url compliant
 */
abstract class ConfigView<V : View>(
    val name: String,
    val label: String,
    val viewFunc: KClass<out V>,
    val baseUrl: String = viewFunc.simpleName!!
) {
    companion object {
        val configViewMap = mutableMapOf<String, ConfigView<*>>()
    }

    val url: String = navigoPrefix + this.baseUrl
    val labelUrl: Pair<String, String> = label to url

    /**
     * builds a single pair of key=value url parameter
     */
    inline fun <reified T> pairParam(key: String, obj: T): Pair<String, String> =
        key to encodeURIComponent(btoa(Json.encodeToString(obj)))

    fun <T> pairParam(key: String, serializer: KSerializer<T>, obj: T): Pair<String, String> =
        key to encodeURIComponent(btoa(Json.encodeToString(serializer, obj)))

    /**
     * builds a url with a list of pair values of key=value url parameters
     */
    fun urlWithParams(vararg pairParams: Pair<String, String>): String {
        return if (pairParams.isNotEmpty()) {
            val result = StringBuilder(url)
            pairParams.forEachIndexed { i, s ->
                result.append(if (i == 0) "?" else "&")
                result.append("${s.first}=${s.second}")
            }
            result.toString()
        } else {
            url
        }
    }

    init {
        if (this !is ConfigViewContainer<*, *, *>) {
            configViewMap[baseUrl] = this
        }
    }
}

@Suppress("unused")
fun String.rh() = this.removePrefix("#/")

@Suppress("unused")
fun <V : View> configView(
    name: String,
    label: String,
    viewFunc: KClass<out V>,
    baseUrl: String = viewFunc.simpleName!!
): ConfigView<V> = object : ConfigView<V>(
    name = name,
    label = label,
    viewFunc = viewFunc,
    baseUrl = baseUrl,
) {}
