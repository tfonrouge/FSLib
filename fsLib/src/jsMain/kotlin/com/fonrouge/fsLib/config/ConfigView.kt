package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.view.View
import com.fonrouge.fsLib.view.ViewDataContainer
import io.kvision.utils.createInstance
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

private const val navigoPrefix = "#/"

/*
    TODO: encode/decode baseUrl to be url compliant
 */
abstract class ConfigView<CC : ICommon<FILT>, V : View<CC, FILT>, FILT : IApiFilter<*>>(
    val viewKClass: KClass<out V>,
    open val commonContainer: CC,
    internal val _baseUrl: String? = null,
) {
    open val baseUrl: String
        get() {
            return _baseUrl ?: ("View" + commonContainer.name)
        }

    companion object {
        val configViewMap = mutableMapOf<String, ConfigView<*, *, *>>()
    }

    val url: String get() = navigoPrefix + this.baseUrl
    open val label: String get() = commonContainer.label
    open val labelUrl: Pair<String, String> by lazy { commonContainer.label to url }

    /**
     * Helper function to create a new View instance, in [ViewDataContainer] sets the [ViewDataContainer.apiFilterObservable] from the [UrlParams]
     */
    fun newViewInstance(urlParams: UrlParams?): V {
        return viewKClass.js.createInstance(urlParams)
    }

    /**
     * builds a single pair of key=value url parameter
     */
    inline fun <reified T> pairParam(key: String, obj: T): Pair<String, String> =
        key to Json.encodeToString(obj)

    fun <T> pairParam(key: String, serializer: KSerializer<T>, obj: T): Pair<String, String> =
        key to Json.encodeToString(serializer, obj)

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
    fun apiFilterParam(obj: FILT): Pair<String, String> =
        pairParam(key = "apiFilter", serializer = commonContainer.apiFilterSerializer, obj = obj)

    init {
        if (this !is ConfigViewContainer<*, *, *, *, *>) {
            configViewMap[this.baseUrl] = this
        }
    }
}

@Suppress("unused")
inline fun <CC : ICommon<FILT>, V : View<CC, FILT>, reified FILT : IApiFilter<*>> configView(
    viewKClass: KClass<out V>,
    commonContainer: CC,
    baseUrl: String? = null,
): ConfigView<CC, V, FILT> = object : ConfigView<CC, V, FILT>(
    viewKClass = viewKClass,
    commonContainer = commonContainer,
    _baseUrl = baseUrl,
) {}
