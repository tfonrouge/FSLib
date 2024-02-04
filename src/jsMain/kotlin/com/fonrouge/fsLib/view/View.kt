package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.config.ConfigView
import com.fonrouge.fsLib.config.ICommon
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
import io.kvision.utils.em
import io.kvision.utils.px

abstract class View<CV : ICommon<FILT>, FILT : IApiFilter>(
    var urlParams: UrlParams? = null,
    open val configView: ConfigView<CV, *, FILT>,
    var editable: Boolean = true,
    val icon: String? = null,
) {
    open val label: String get() = configView.label
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
    val apiFilterObservableValue: ObservableValue<FILT> by lazy {
        ObservableValue(apiFilterInstance(apiFilterFromUrl) ?: configView.commonView.apiFilterInstance())
    }
    var apiFilter: FILT
        get() {
            return apiFilterObservableValue.value
        }
        set(value) {
            apiFilterObservableValue.value = value
        }

    protected val apiFilterFromUrl: FILT?
        get() = urlParams?.pullUrlParam(
            serializer = configView.commonView.apiFilterSerializer,
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
    fun Container.add(view: View<*, *>): Container {
        view.apply {
            displayPage()
        }
        return this
    }

    /**
     * Helper to build an [ApiItem] instance
     */
    @Suppress("unused")
    fun <T : BaseDoc<ID>, ID : Any> apiItem(
        id: ID? = null,
        item: T? = null,
        callType: ApiItem.CallType = ApiItem.CallType.Query,
        crudTask: CrudTask = CrudTask.Read,
        apiFilter: FILT = configView.commonView.apiFilterInstance(),
    ): ApiItem<T, ID, FILT> {
        return ApiItem(
            id = id,
            item = item,
            callType = callType,
            crudTask = crudTask,
            apiFilter = apiFilter
        )
    }

    /**
     * Sets the current browser url with an [apiFilterObservableValue] url parameter
     */
    fun apiFilterToUrl() {
        configView.pairParam("apiFilter", configView.commonView.apiFilterSerializer, apiFilter)
            .let { pair ->
                urlParams?.params?.set(pair.first, pair.second)
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
     * Note: don't try to update [apiFilterObservableValue] inside this, or you'll get a recursive infinite loop
     */
    open fun Container.bannerLegend() {}
    abstract fun Container.displayPage()
    open fun onAfterDisplayPage() {}

    /**
     * Build an [FILT] class for the current view
     *
     * @param apiFilter [FILT] object decoded from the current url 'apiFilter' param view
     * @return a [FILT] object
     */
    open fun apiFilterInstance(apiFilter: FILT?): FILT? = apiFilter
    open fun onApiFilterUpdate() {
        updateBanner()
    }

    open fun onBeforeDisplayPage(container: Container) {}

    open fun onBeforeDispose() {}

    /**
     * Contains the [linkBanner] where is the main label and the banner legend zone [bannerLegend]
     */
    open fun Container.bannerPanel(): Container = hPanel(alignItems = AlignItems.CENTER, className = "container-fluid")

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
                if (this@View is ViewItem<*, *, *, *>) {
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
        pageBannerLink?.label = "${configView.commonView.label}: $text"
        pageBannerLink?.url = "${configView.url}/$url"
    }

    /**
     * open function that builds a filter form
     */
    open fun Container.buildOffCanvasFilterView(): Offcanvas? = null

    /**
     * open function that fires when toolbar's filter button is clicked. If [apiFilterObservableValue] contains a null value then
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
