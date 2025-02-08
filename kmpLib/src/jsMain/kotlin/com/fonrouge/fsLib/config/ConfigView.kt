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
 * Abstract class representing a configuration view within the application.
 * A generic base for defining a view that is associated with a specific configuration
 * and common container. It provides utilities for managing URLs, parameters,
 * and creating new instances of the associated view.
 *
 * @param CC The common container type that extends `ICommon`.
 * @param V The specific type of the `View` associated with this configuration view.
 * @param FILT The filter type utilized in the configuration, extending `IApiFilter`.
 * @property configData Contains configuration-related data including the common container and filters.
 * @property viewKClass The Kotlin class instance representing the associated view.
 * @property _baseUrl Optional base URL for the view, defaulting to null.
 */
abstract class ConfigView<CC : ICommon<FILT>, V : View<CC, FILT>, FILT : IApiFilter<*>>(
    open val configData: ConfigData<CC, FILT>,
    val viewKClass: KClass<out V>,
    internal val _baseUrl: String? = null,
) {
    open val baseUrl: String
        get() {
            return _baseUrl ?: "View${configData.commonContainer.name}"
        }

    companion object {
        val configViewMap = mutableMapOf<String, ConfigView<*, *, *>>()
    }

    val url: String get() = "#/" + this.baseUrl
    open val label: String get() = configData.commonContainer.label
    open val labelUrl: Pair<String, String> by lazy { configData.commonContainer.label to url }

    /**
     * Helper function to create a new View instance, in [ViewDataContainer] sets the [ViewDataContainer.apiFilterObservable] from the [UrlParams]
     */
    fun newViewInstance(urlParams: UrlParams?): V {
        return viewKClass.js.createInstance(urlParams)
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
        pairParam(key = "apiFilter", serializer = configData.commonContainer.apiFilterSerializer, obj = obj)

    init {
        if (this !is ConfigViewContainer<*, *, *, *, *>) {
            configViewMap[this.baseUrl] = this
        }
    }
}

/**
 * Configures a view instance of the provided type `V` tied to a common container of type `CC` and
 * optionally a base URL. The configuration supports filters of type `FILT`.
 *
 * @param viewKClass The [KClass] representing the type of the view to be configured.
 * @param commonContainer The common container instance of type `CC` which implements [ICommon].
 * @param baseUrl An optional base URL as a string utilized for configuration. Defaults to `null`.
 * @return An instance of [ConfigView] parameterized with the types `CC`, `V`, and `FILT`, which encapsulates the setup.
 */
@Suppress("unused")
inline fun <CC : ICommon<FILT>, V : View<CC, FILT>, reified FILT : IApiFilter<*>> configView(
    viewKClass: KClass<out V>,
    commonContainer: CC,
    baseUrl: String? = null,
): ConfigView<CC, V, FILT> = object : ConfigView<CC, V, FILT>(
    configData = configData<CC, FILT>(
        commonContainer = commonContainer,
    ),
    viewKClass = viewKClass,
    _baseUrl = baseUrl,
) {}
