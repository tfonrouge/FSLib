package com.fonrouge.fullStack.config

import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.common.ICommon
import com.fonrouge.base.lib.UrlParams
import com.fonrouge.base.lib.encodeURIComponent
import com.fonrouge.fullStack.view.View
import io.kvision.utils.createInstance
import kotlinx.browser.window
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.w3c.dom.Window
import kotlin.reflect.KClass
import kotlin.reflect.createInstance

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
     * Opens a new browser window or tab with the URL generated from the current configuration.
     *
     * If the current instance is of type `ConfigViewList`, a specialized URL for the view list
     * is generated; otherwise, a general URL with API filter parameters is constructed.
     *
     * @param apiFilter The API filter instance used to build the URL. Defaults to the result of `commonContainer.apiFilterInstance()`.
     * @param target Specifies where to open the new window or tab. Defaults to "_blank", which opens it in a new tab or window.
     * @return A `Window` object representing the newly opened window or tab, or `null` if the operation is unsuccessful (e.g., blocked by a popup blocker).
     */
    @Suppress("unused")
    fun navigateTo(
        apiFilter: FILT = commonContainer.apiFilterInstance(),
        target: String = "_blank",
    ): Window? = window.open(
        url = toLabelUrlPair(apiFilter).second,
        target = target
    )

    /**
     * Creates a new instance of the view and initializes it with the provided URL parameters and initialization logic.
     *
     * @param urlParams The optional URL parameters to be associated with the new view instance. If not provided, default URL parameters are used.
     * @param init An optional lambda function used to initialize the view instance after its creation.
     * @return A newly created view instance of type [V], configured with the provided parameters and initialization logic.
     */
    @OptIn(ExperimentalJsReflectionCreateInstance::class)
    fun newViewInstance(urlParams: UrlParams?, init: (V.() -> Unit)? = null): V {
        val v1: V = viewKClass.createInstance()
        // TODO: find out why are differences between js.createInstance() and kotlin.reflect.createInstance() in development mode vs production mode
        val v: V = if (js("typeof v1") == "object") {
//            console.warn("using kotlin.reflect.createInstance() instead of js.createInstanceHack() for ${viewKClass.simpleName}")
            v1
        } else {
//            console.warn("using js.createInstanceHack().unsafeCast<V> for ${viewKClass.simpleName}")
            viewKClass.js.createInstance()
        }
//        console.warn("v", v)
        return v.apply {
            this.urlParams = urlParams ?: UrlParams()
            apiFilterFromUrl?.let {
                apiFilter = it
            }
            init?.invoke(this)
        }
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
     * Generates a pair representing the label and its associated URL based on the current configuration.
     *
     * If the instance is of type `ConfigViewList`, a specialized view list URL is constructed.
     * Otherwise, a general URL including API filter parameters is created.
     *
     * @param apiFilter The API filter instance used to build the URL. Defaults to the result of `commonContainer.apiFilterInstance()`.
     * @return A `Pair` containing the label as the first value and the constructed URL as the second value.
     */
    open fun toLabelUrlPair(
        apiFilter: FILT = commonContainer.apiFilterInstance()
    ): Pair<String, String> = label to viewUrl(apiFilter)

    /**
     * builds an url with a list key=value url parameters
     */
    fun urlWithParams(vararg pairParams: Pair<String, String>): String = if (pairParams.isNotEmpty()) {
        val result = StringBuilder(url)
        pairParams.forEachIndexed { i, s ->
            result.append(if (i == 0) "?" else "&")
            result.append("${s.first}=${encodeURIComponent(s.second)}")
        }
        result.toString()
    } else {
        url
    }

    /**
     * Generates a URL based on the current configuration and specified API filter.
     *
     * - If the current instance is of type `ConfigViewList`, a specialized URL for the view list is generated.
     * - Otherwise, a general URL with API filter parameters is constructed.
     *
     * @param apiFilter The API filter instance used to build the URL.
     * @return A string representing the constructed URL.
     */
    fun viewUrl(apiFilter: FILT): String =
        if (this is ConfigViewList<*, *, *, *, FILT, *, *>) {
            viewListUrl(apiFilter)
        } else {
            urlWithParams(apiFilterParam(apiFilter))
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
