package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.view.View
import com.fonrouge.fsLib.view.ViewDataContainer
import io.kvision.utils.createInstance
import js.uri.encodeURIComponent
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

private const val navigoPrefix = "#/"

/*
    TODO: encode/decode baseUrl to be url compliant
 */
abstract class ConfigView<V : View<FILT>, FILT : IApiFilter>(
    val viewFunc: KClass<out V>,
    val apiFilterKClass: KClass<FILT>,
    val baseUrl: String = viewFunc.simpleName!!,
    val requireCredentials: Boolean,
) {
    abstract val commonView: ICommonView<FILT>

    companion object {
        val configViewMap = mutableMapOf<String, ConfigView<*, *>>()
    }

    val url: String = navigoPrefix + this.baseUrl
    val labelUrl: Pair<String, String> by lazy { commonView.label to url }

    /**
     * Helper function to create a new View instance, in [ViewDataContainer] sets the [ViewDataContainer.apiFilter] from the [UrlParams]
     */
    fun newViewInstance(urlParams: UrlParams?): V {
        val view = viewFunc.js.createInstance<V>()
        view.urlParams = urlParams
        return view
    }

    /**
     * builds a single pair of key=value url parameter
     */
    inline fun <reified T> pairParam(key: String, obj: T): Pair<String, String> =
        key to encodeURIComponent(Json.encodeToString(obj))

    fun <T> pairParam(key: String, serializer: KSerializer<T>, obj: T): Pair<String, String> =
        key to encodeURIComponent(Json.encodeToString(serializer, obj))

    /**
     * builds an url with a list of pair values of key=value url parameters
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

    /**
     * helper to build an api filter parameter in the url string
     */
    @OptIn(InternalSerializationApi::class)
    @Suppress("unused")
    fun apiFilterParam(obj: FILT): Pair<String, String> =
        pairParam(key = "apiFilter", serializer = apiFilterKClass.serializer(), obj = obj)

    init {
        if (this !is ConfigViewContainer<*, *>) {
            configViewMap[baseUrl] = this
        }
    }
}

@Suppress("unused")
fun String.rh() = this.removePrefix("#/")

@Suppress("unused")
inline fun <V : View<FILT>, reified FILT : IApiFilter> configView(
    name: String,
    viewFunc: KClass<out V>,
    apiFilterKClass: KClass<FILT> = FILT::class,
    baseUrl: String = viewFunc.simpleName!!,
    requireCredentials: Boolean = true,
    commonView: ICommonView<FILT>,
): ConfigView<V, FILT> = object : ConfigView<V, FILT>(
//    name = name,
    viewFunc = viewFunc,
    apiFilterKClass = apiFilterKClass,
    baseUrl = baseUrl,
    requireCredentials = requireCredentials,
) {
    override var commonView: ICommonView<FILT> = commonView
}
