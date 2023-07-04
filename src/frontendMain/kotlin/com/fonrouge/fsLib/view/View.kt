package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.config.ConfigView
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.lib.iconCrud
import com.fonrouge.fsLib.model.apiData.ApiFilter
import io.kvision.core.BsBgColor
import io.kvision.core.Container
import io.kvision.html.*
import io.kvision.navbar.Navbar
import io.kvision.navbar.nav
import io.kvision.navbar.navbar
import io.kvision.state.ObservableValue
import io.kvision.state.bind
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import io.kvision.utils.em
import io.kvision.utils.px
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

abstract class View<FILT : ApiFilter>(
    var urlParams: UrlParams? = null,
    open val configView: ConfigView<*, FILT>,
    var editable: Boolean = true,
    val icon: String? = null,
    open val label: String = configView.label
) {
    open var labelBanner: String?
        get() {
            return linkBanner?.label
        }
        set(value) {
            if (value != null) {
                linkBanner?.label = value
            }
        }
    var linkBanner: Link? = null
    var navbar: Navbar? = null
    var navButtonCancel: Button? = null
    var navButtonAccept: Button? = null
    var navButtonBack: Button? = null
    val navigoUrlWithParams: String
        get() {
            return configView.url + if (urlParams != null) urlParams else ""
        }

    /**
     * Set to true to don't display the [pageBanner]
     */
    var noPageBanner = false
    var pageBannerLink: Link? = null

    /**
     * Enable/disable periodic refresh interval of view
     */
    open val periodicUpdateDataView: Boolean? = null
    var periodicUpdateViewInterval = 5
    private val pageBannerUpdateObservable = ObservableValue(0)

    /**
     * observable that contains an [FILT] object. It can be assigned from an apiFilter= url parameter
     * or programmatically, and it's delivered to the backend
     */
    val apiFilter: ObservableValue<FILT> by lazy {
        ObservableValue(apiFilterFromUrl ?: newApiFilterInstance())
    }

    @OptIn(InternalSerializationApi::class)
    protected val apiFilterFromUrl: FILT?
        get() = urlParams?.pullUrlParam(
            serializer = configView.apiFilterKClass.serializer(),
            key = "apiFilter"
        )

    /**
     * Allows to insert the whole view to the current DSL container
     */
    fun Container.add(view: View<*>): Container {
        view.apply {
            displayPage()
        }
        return this
    }

    /**
     * Sets the current browser url with an [apiFilter] url parameter
     */
    @OptIn(InternalSerializationApi::class)
    fun apiFilterToUrl() {
        val pair = configView.pairParam("apiFilter", configView.apiFilterKClass.serializer(), apiFilter.value)
        urlParams?.params?.set(pair.first, pair.second)
        @Suppress("UNUSED_VARIABLE")
        val url = (configView.url + urlParams.toString()).asDynamic()

        @Suppress("UNUSED_VARIABLE")
        val stateObj =
            "{apiFilter: toUrl}".asDynamic()
        js("""history.replaceState(stateObj,"createToUpdate",url)""")
    }

    /**
     * Allows to describe a display that will be showed next to the view link banner
     * it can be triggered with [updateBanner] function
     */
    open fun Container.bannerLegend() {

    }

    abstract fun Container.displayPage()

    /**
     * Allows to set an initial [apiFilter] value if it can't be obtained from [urlParams]
     */
    open suspend fun initialApiFilter(): FILT? = null

    /**
     * Builds a new instance of [apiFilter]
     */
    @OptIn(InternalSerializationApi::class)
    open fun newApiFilterInstance(): FILT {
        return try {
            Json.decodeFromString(configView.apiFilterKClass.serializer(), """{}""")
        } catch (e: Exception) {
            val errMsg = """
                Error creating instance of apiFilter,
                hint: [${configView.apiFilterKClass.simpleName}]::class must *not* have constructor parameters,
                or need to override the newApiFilterInstance() function
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

    open fun onAfterDisplayPage() {
        if (apiFilterFromUrl == null)
            AppScope.launch {
                initialApiFilter()?.let {
                    apiFilter.value = it
                }
            }
    }

    open fun onApiFilterUpdate() {
        updateBanner()
    }

    open fun onBeforeDisplayPage(container: Container) {}

    open fun onBeforeDispose() {}

    fun Container.pageBanner(onUpdatePageBannerLink: ((Link) -> Unit)? = null) {
        /* TODO: find out how make horizontally scrollable */
        navbar(bgColor = BsBgColor.LIGHT).bind(
            observableState = pageBannerUpdateObservable,
            removeChildren = true
        ) {
            linkBanner = link(
                label = this@View.label,
                url = navigoUrlWithParams,
                className = "navbar-brand",
                icon = iconCrud(urlParams?.crudTask)
            )
            bannerLegend()
            nav(rightAlign = true) {
                if (this@View is ViewItem<*, *, *>) {
                    if (urlParams?.actionUpsert == true) {
                        navButtonBack = button(text = " ", icon = "fas fa-reply", style = ButtonStyle.OUTLINEPRIMARY) {
                            hide()
                            onClick {
                                this@View.backCloseAction()
                            }
                        }
                        navButtonCancel = button(text = " ", icon = "fas fa-xmark", style = ButtonStyle.OUTLINEDANGER) {
                            fontSize = 0.5.em
                            onClick {
                                this@View.backCloseAction(confirmCancel = true)
                            }
                        }
                        navButtonAccept =
                            button(text = " ", icon = "fas fa-check", style = ButtonStyle.OUTLINESUCCESS) {
                                fontSize = 0.5.em
                                marginLeft = 5.px
                                onClick {
                                    this@View.acceptUpsertAction()
                                }
                            }
                    }
                }
            }
            onUpdatePageBannerLink?.let {
//                onUpdatePageBannerLink = it
                pageBannerLink?.let { link -> onUpdatePageBannerLink(link) }
            }
            marginBottom = 1.em
        }
    }

    /**
     * Makes a refresh on the banner legend with content of [bannerLegend] function
     */
    fun updateBanner() {
        pageBannerUpdateObservable.value++
    }

    fun updateMainBannerLink(text: String, url: String) {
        pageBannerLink?.label = "${configView.label}: $text"
        pageBannerLink?.url = "${configView.url}/$url"
    }

    /**
     * Builds an url with an [apiFilter] parameter value
     *
     * @param configView - The [ConfigViewList] of the [ViewList] to go
     */
    fun <F : ApiFilter> urlApiFilter(
        configView: ConfigView<*, F>,
        apiFilter: F,
    ): String {
        val params = mutableListOf<Pair<String, String>>()
        params.add(configView.apiFilterParam(apiFilter))
        return configView.urlWithParams(*params.toTypedArray())
    }
}
