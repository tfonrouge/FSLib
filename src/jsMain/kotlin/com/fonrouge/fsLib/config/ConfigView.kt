package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.view.View
import com.fonrouge.fsLib.view.ViewDataContainer
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import io.kvision.utils.createInstance
import js.uri.encodeURIComponent
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

private const val navigoPrefix = "#/"

/*
    TODO: encode/decode baseUrl to be url compliant
 */
abstract class ConfigView<CV : ICommon<FILT>, V : View<CV, FILT>, FILT : IApiFilter>(
    val viewFunc: KClass<out V>,
    open val commonView: CV,
    internal val _baseUrl: String? = null,
) {
    open val baseUrl: String
        get() {
            val result =
                _baseUrl ?: if (commonView == undefined) "error: commonView undefined" else ("View" + commonView.name)
            return result
        }

    companion object {
        val configViewMap = mutableMapOf<String, ConfigView<*, *, *>>()
    }

    val url: String get() = navigoPrefix + this.baseUrl
    open val label: String get() = commonView.label
    open val labelUrl: Pair<String, String> by lazy { commonView.label to url }

    /**
     * Builds a new instance of [FILT]
     */
    open fun apiFilterInstance(): FILT {
        return try {
            Json.decodeFromString(commonView.apiFilterSerializer, """{}""")
        } catch (e: SerializationException) {
            val errMsg = """
                Error creating instance of apiFilter: ${e.message},
                hint: Set @Serializable annotation to [${commonView.apiFilterSerializer}]::class,
                """.trimIndent()
            e.message
            console.error(errMsg)
            Toast.danger(
                message = errMsg,
                options = ToastOptions(
                    position = ToastPosition.BOTTOMRIGHT,
                    escapeHtml = true,
                    duration = 10000,
                    stopOnFocus = true,
                    newWindow = true
                )
            )
            throw e
        } catch (e: Exception) {
            val errMsg = """
                Error creating instance of apiFilter,
                hint: [${commonView.apiFilterSerializer}]::class must *not* have required constructor parameters,
                or need to override the onNewApiFilterInstance() function
                """.trimIndent()
            e.message
            console.error(errMsg)
            Toast.danger(
                message = errMsg,
                options = ToastOptions(
                    position = ToastPosition.BOTTOMRIGHT,
                    escapeHtml = true,
                    duration = 10000,
                    stopOnFocus = true,
                    newWindow = true
                )
            )
            throw e
        }
    }

    /**
     * Helper function to create a new View instance, in [ViewDataContainer] sets the [ViewDataContainer.apiFilterObservableValue] from the [UrlParams]
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
    @Suppress("unused")
    fun apiFilterParam(obj: FILT): Pair<String, String> =
        pairParam(key = "apiFilter", serializer = commonView.apiFilterSerializer, obj = obj)

    init {
        if (this !is ConfigViewContainer<*, *, *, *, *>) {
            configViewMap[this.baseUrl] = this
        }
    }
}

@Suppress("unused")
fun String.rh() = this.removePrefix("#/")

@Suppress("unused")
inline fun <CV : ICommon<FILT>, V : View<CV, FILT>, reified FILT : IApiFilter> configView(
    viewFunc: KClass<out V>,
    commonView: CV,
    baseUrl: String? = null,
): ConfigView<CV, V, FILT> = object : ConfigView<CV, V, FILT>(
    viewFunc = viewFunc,
    commonView = commonView,
    _baseUrl = baseUrl,
) {}
