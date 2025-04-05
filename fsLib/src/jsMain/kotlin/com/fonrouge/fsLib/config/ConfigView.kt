package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.common.ICommon
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.view.View
import com.fonrouge.fsLib.view.ViewDataContainer
import io.kvision.utils.createInstance
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

/**
 * Abstract class that represents a configuration view for managing connections between common containers,
 * views, and API filters. This class provides utility methods for URL management and API filter handling.
 *
 * @param CC The type of the common container implementing ICommon.
 * @param V The type of the associated view.
 * @param FILT The type of the API filter.
 * @property commonContainer The common container defining shared data, filter serializers, and properties such as labels.
 * @property viewKClass The Kotlin class reference for the associated view.
 * @property _baseUrl The optional base URL for this configuration view.
 */
abstract class ConfigView<out CC : ICommon<FILT>, V : View<CC, FILT>, FILT : IApiFilter<*>>(
    val commonContainer: CC,
    val viewKClass: KClass<out V>,
    @Suppress("PropertyName") internal val _baseUrl: String? = null,
) {
    open val baseUrl: String
        get() {
            return _baseUrl ?: "View${commonContainer.name}"
        }

    companion object {
        val configViewMap = mutableMapOf<String, ConfigView<*, *, *>>()
    }

    val url: String get() = "#/" + this.baseUrl
    open val label: String get() = commonContainer.label
    open val labelUrl: Pair<String, String> by lazy { commonContainer.label to url }

    /**
     * Helper function to create a new View instance, in [ViewDataContainer] sets the [ViewDataContainer.apiFilterObservable] from the [UrlParams]
     */
    fun newViewInstance(urlParams: UrlParams?, vararg args: dynamic): V {
        return viewKClass.js.createInstance(urlParams ?: UrlParams(), args)
    }

    /**
     * builds a single pair of key=value url parameter
     */
    @Suppress("unused")
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

/**
 * Configures and returns a `ConfigView` instance using the provided view class, common container, and optional base URL.
 *
 * @param viewKClass The `KClass` reference of the View class to be configured.
 * @param commonContainer The instance of the `ICommon` implementation containing shared parameters and logic.
 * @param baseUrl An optional base URL for the configuration. This is nullable and defaults to `null` if not provided.
 * @return A `ConfigView` instance configured with the specified parameters and types.
 */
@Suppress("unused")
inline fun <CC : ICommon<FILT>, V : View<CC, FILT>, reified FILT : IApiFilter<*>> configView(
    viewKClass: KClass<out V>,
    commonContainer: CC,
    baseUrl: String? = null,
): ConfigView<CC, V, FILT> = object : ConfigView<CC, V, FILT>(
    commonContainer = commonContainer,
    viewKClass = viewKClass,
    _baseUrl = baseUrl,
) {}
