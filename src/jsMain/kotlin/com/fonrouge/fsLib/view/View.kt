package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.config.ConfigView
import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.lib.iconCrud
import com.fonrouge.fsLib.model.CrudTask
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import io.kvision.core.*
import io.kvision.html.*
import io.kvision.navbar.Navbar
import io.kvision.navbar.nav
import io.kvision.navbar.navbar
import io.kvision.offcanvas.Offcanvas
import io.kvision.panel.hPanel
import io.kvision.state.ObservableValue
import io.kvision.state.bind
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import io.kvision.utils.em
import io.kvision.utils.px
import js.uri.encodeURIComponent
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

abstract class View<FILT : IApiFilter>(
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
    val apiFilter: ObservableValue<FILT?> by lazy {
        ObservableValue(apiFilterFromUrl ?: onNewApiFilterInstance())
    }

    @OptIn(InternalSerializationApi::class)
    protected val apiFilterFromUrl: FILT?
        get() = urlParams?.pullUrlParam(
            serializer = configView.apiFilterKClass.serializer(),
            key = "apiFilter"
        )

    /**
     * assignable var that contains a defined [Offcanvas] filter area, if any
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var offCanvasFilter: Offcanvas? = null

    /**
     * assignable var that indicates if the filter button in the tabulator's toolbar will be displayed
     */
    var hasOffCanvasFilterView: Boolean = false

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
        apiFilter.value?.let { apiFilter ->
            configView.pairParam("apiFilter", configView.apiFilterKClass.serializer(), apiFilter).let { pair ->
                urlParams?.params?.set(pair.first, pair.second)
            }
        }
        @Suppress("UNUSED_VARIABLE")
        val url = (configView.url + urlParams.toString()).asDynamic()

        @Suppress("UNUSED_VARIABLE")
        val stateObj =
            "{apiFilter: toUrl}".asDynamic()
        js("""history.replaceState(stateObj,"createToUpdate",url)""")
    }

    /**
     * Allows to describe a display that will be showed next to the view link banner
     * it can be triggered with [updateBanner] function.
     * Note: don't try to update [apiFilter] inside this, or you'll get a recursive infinite loop
     */
    open fun Container.bannerLegend() {}
    abstract fun Container.displayPage()

    /**
     * Builds a new instance of [apiFilter]
     */
    @OptIn(InternalSerializationApi::class)
    open fun onNewApiFilterInstance(): FILT? {
        return try {
            Json.decodeFromString(configView.apiFilterKClass.serializer(), """{}""")
        } catch (e: SerializationException) {
            val errMsg = """
                Error creating instance of apiFilter: ${e.message},
                hint: Set @Serializable annotation to [${configView.apiFilterKClass}]::class,
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
                hint: [${configView.apiFilterKClass}]::class must *not* have required constructor parameters,
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

    open fun onAfterDisplayPage() {}
    open fun onApiFilterUpdate() {
        updateBanner()
    }

    open fun onBeforeDisplayPage(container: Container) {}

    open fun onBeforeDispose() {}

    /**
     * Contains the [linkBanner] where is the main label and the banner legend zone [bannerLegend]
     */
    open fun Container.bannerPanel(): Container = hPanel(alignItems = AlignItems.CENTER)

    fun Container.pageBanner(onUpdatePageBannerLink: ((Link) -> Unit)? = null) {
        /* TODO: find out how make horizontally scrollable */
        navbar(bgColor = BsBgColor.LIGHT).bind(
            observableState = pageBannerUpdateObservable,
            removeChildren = true
        ) {
            bannerPanel().apply {
                linkBanner = link(
                    label = this@View.label,
                    url = navigoUrlWithParams,
                    className = "navbar-brand",
                    icon = iconCrud(urlParams?.crudTask)
                )
                bannerLegend()
            }
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
        hasOffCanvasFilterView = buildOffCanvasFilterView()?.let {
            offCanvasFilter = it
            true
        } ?: false
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
     * @param configView - The [ConfigView] of the [View] to go
     */
    fun <F : IApiFilter> urlApiFilter(
        configView: ConfigView<*, F>,
        apiFilter: F,
    ): String {
        val params = mutableListOf<Pair<String, String>>()
        configView.apiFilterParam(apiFilter).let { params.add(it) }
        return configView.urlWithParams(*params.toTypedArray())
    }

    /**
     * open function that builds a filter form
     */
    open fun Container.buildOffCanvasFilterView(): Offcanvas? = null

    /**
     * open function that fires when toolbar's filter button is clicked. If [apiFilter] contains a null value then
     * a new [FILT] object is created (with no constructor parameters) and assign it to the apiFilter value.
     */
    open fun onClickFilter() {
        offCanvasFilter?.show()
    }

    fun Widget.onClickShowOffcanvasFilterView() {
        cursor = Cursor.POINTER
        onClick {
            offCanvasFilter?.show()
        }
    }
}

/**
 * Builds a string Url based on a [ConfigViewItem] and a [ApiItem] parameters
 * @return Url string
 */
@OptIn(InternalSerializationApi::class)
fun <T : BaseDoc<ID>, ID : Any, F : IApiFilter> urlApiItem(
    configViewItem: ConfigViewItem<*, ID, *, *, F>,
    apiItem: ApiItem<T, ID, F>
): String? {
    val url: String? = when (apiItem.crudTask) {
        CrudTask.Create -> listOf("action" to CrudTask.Create.name)
        else -> {
            apiItem.item?._id?.let {
                listOf(
                    "action" to apiItem.crudTask.name,
                    "id" to Json.encodeToString(configViewItem.idKClass.serializer(), it)
                )
            }
        }
    }?.let { params ->
        val urlParams = UrlParams(*params.toTypedArray())
        apiItem.apiFilter?.let {
            urlParams.pushParam(
                "apiFilter" to encodeURIComponent(
                    Json.encodeToString(
                        configViewItem.apiFilterKClass.serializer(),
                        apiItem.apiFilter
                    )
                )
            )
        }
        configViewItem.url + urlParams.toString()
    }
    return url
}
