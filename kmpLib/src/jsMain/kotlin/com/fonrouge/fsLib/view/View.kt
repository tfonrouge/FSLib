package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.config.ConfigView
import com.fonrouge.fsLib.config.ICommon
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.lib.iconCrud
import com.fonrouge.fsLib.lib.toEncodedUrlString
import com.fonrouge.fsLib.model.apiData.IApiFilter
import io.kvision.core.AlignItems
import io.kvision.core.BsBgColor
import io.kvision.core.Container
import io.kvision.html.*
import io.kvision.navbar.nav
import io.kvision.navbar.navbar
import io.kvision.offcanvas.Offcanvas
import io.kvision.panel.hPanel
import io.kvision.state.ObservableValue
import io.kvision.state.bind
import io.kvision.utils.em
import io.kvision.utils.px
import kotlinx.coroutines.launch

abstract class View<CC : ICommon<FILT>, FILT : IApiFilter<*>>(
    open val configView: ConfigView<CC, *, FILT>,
    var editable: (() -> Boolean) = { true },
) {
    abstract var urlParams: UrlParams?
    open val label: String get() = configView.label
    var linkBanner: Link? = null
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
     * Indicates whether the data view should perform periodic updates.
     *
     * This boolean flag is used to enable or disable automatic periodic updates
     * for the data view. When set to true, the data view will periodically
     * refresh its content according to the specified interval.
     *
     * It can be particularly useful in scenarios where real-time data
     * synchronization is required or to ensure that the information displayed
     * is always up-to-date.
     *
     * The default value is null, which means periodic updates are not configured.
     */
    open val periodicUpdateDataView: Boolean? = null

    /**
     * The interval, in seconds, at which the view will be periodically updated.
     * This variable determines how often the view's data will be refreshed.
     *
     * Default value is set to 5 seconds.
     */
    var periodicUpdateViewInterval = 5
    private val pageBannerUpdateObservable = ObservableValue(0)

    /**
     * Observable value that represents the current API filter instance used by the view.
     * This value is lazily initialized with an API filter instance based on the URL parameters or,
     * if not available, from the commonContainer configuration of the configView.
     *
     * The primary use of this observable is to keep track of changes to the API filter
     * and trigger related updates within the view, such as updating the displayed content
     * or the status of UI elements that depend on the filter.
     */
    val apiFilterObservable: ObservableValue<FILT> by lazy {
        ObservableValue(
            apiFilterInstance(apiFilterFromUrl) ?: configView.commonContainer.apiFilterInstance()
        )
    }

    /**
     * A variable representing the current API filter used within the view.
     *
     * The `apiFilter` is observable and can be set or retrieved programmatically. It's primarily used
     * for filtering API data based on specific parameters defined by the `FILT` type.
     */
    var apiFilter: FILT
        get() {
            return apiFilterObservable.value
        }
        set(value) {
            apiFilterObservable.value = value
        }

    protected val apiFilterFromUrl: FILT?
        get() = urlParams?.pullUrlParam(
            serializer = configView.commonContainer.apiFilterSerializer,
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
     * Sets the current browser url with an [apiFilterObservable] url parameter
     */
    fun apiFilterToUrl() {
        configView.pairParam("apiFilter", configView.commonContainer.apiFilterSerializer, apiFilter)
            .let { pair ->
                urlParams?.params?.set(pair.first, pair.second)
            }
        @Suppress("UNUSED_VARIABLE")
        val url = (configView.url + urlParams.toEncodedUrlString()).asDynamic()

        @Suppress("UNUSED_VARIABLE")
        val stateObj =
            "{apiFilter: toUrl}".asDynamic()
        js("""history.replaceState(stateObj,"createToUpdate",url)""")
    }

    /**
     * Allows to describe a display that will be shown next to the view link banner
     * it can be triggered with [updateBanner] function.
     * Note: don't try to update [apiFilterObservable] inside this, or you'll get a recursive infinite loop
     */
    open fun Container.bannerLegend() {}

    /**
     * Renders the main content of the view within the specified container.
     *
     * This function serves as an abstract method to be implemented by subclasses, defining
     * the logic for displaying the view's content in the given `Container`. It is typically
     * called in the context of setting up or updating the UI.
     */
    abstract fun Container.displayPage()

    /**
     * Retrieves a label for the banner based on a given API filter.
     *
     * @param tag The API filter used to determine the label.
     * @return The label used for the banner.
     */
    open suspend fun labelBanner(apiFilter: FILT): String = label

    /**
     * Updates the label of the link banner.
     *
     * This function asynchronously retrieves the latest label for the
     * banner using the current `apiFilter` and sets it to the `linkBanner`'s label.
     *
     * Note that this function suspends while performing the label retrieval.
     */
    suspend fun updateLabelBanner() {
        linkBanner?.label = labelBanner(apiFilter)
    }

    /**
     * This method is called immediately after rendering the page's content.
     *
     * Override this method to execute any post-rendering logic or to update the UI components
     * that depend on the page's content.
     */
    open fun onAfterDisplayPage() {}

    /**
     * Sets the given API filter instance. This function is useful for assigning an API filter object
     * to a view or passing the filter object around.
     *
     * @param apiFilter The API filter to be set or passed. Can be null.
     * @return The provided API filter instance, or null if no filter is provided.
     */
    open fun apiFilterInstance(apiFilter: FILT?): FILT? = apiFilter

    /**
     * This method is called when there is an update to the API filter.
     *
     * The primary function of this method is to invoke the `updateBanner` method,
     * which refreshes the content of the banner legend tied to the current view.
     *
     * Override this method to implement additional logic that should be executed
     * when the API filter is updated.
     */
    open fun onApiFilterUpdate() {
        updateBanner()
    }

    /**
     * This method is called before rendering the page's content.
     *
     * Override this method to execute any pre-rendering logic or to configure the UI components
     * that should be displayed on the page.
     *
     * @param container The DSL container in which the view will be rendered.
     */
    open fun onBeforeDisplayPage(container: Container) {}

    /**
     * This method is called before the disposal of the view.
     *
     * Override this method to execute any logic that needs to run before the view is disposed of.
     * It can be used to clean up resources, save state, or perform other teardown operations.
     */
    open fun onBeforeDispose() {}

    /**
     * Creates a horizontally aligned panel container with centered items,
     * encapsulated within a fluid container class, typically used
     * for holding the banner legend components.
     *
     * @return the container with specified alignment and class.
     */
    open fun Container.bannerLeggendContainer(): Container =
        hPanel(alignItems = AlignItems.CENTER, className = "container-fluid")

    /**
     * Configures and displays a page banner within the container.
     *
     * @param onUpdatePageBannerLink A lambda function that gets called with the updated link information.
     */
    fun Container.pageBanner(onUpdatePageBannerLink: ((Link) -> Unit)? = null) {
        /* TODO: find out how make horizontally scrollable */
        navbar(bgColor = BsBgColor.LIGHT).bind(
            observableState = pageBannerUpdateObservable,
            removeChildren = true
        ) {
            bannerLeggendContainer().apply {
                linkBanner = link(
                    label = this@View.label,
                    url = navigoUrlWithParams,
                    className = "navbar-brand",
                    icon = iconCrud(urlParams?.crudTask)
                ) {
                    AppScope.launch {
                        label = labelBanner(apiFilter)
                    }
                }
                bannerLegend()
            }
            nav(rightAlign = true) {
                if (this@View is ViewItem<*, *, *, *>) {
                    if (urlParams?.actionUpsert == true) {
                        navButtonBack = button(
                            text = " ",
                            icon = "fas fa-reply",
                            style = ButtonStyle.OUTLINEPRIMARY
                        ) {
                            hide()
                            onClick {
                                this@View.backCloseAction()
                            }
                        }
                        navButtonCancel = button(
                            text = " ",
                            icon = "fas fa-xmark",
                            style = ButtonStyle.OUTLINEDANGER
                        ) {
                            fontSize = 0.5.em
                            onClick {
                                this@View.backCloseAction(confirmCancel = true)
                            }
                        }
                        navButtonAccept =
                            button(
                                text = " ",
                                icon = "fas fa-check",
                                style = ButtonStyle.OUTLINESUCCESS
                            ) {
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
        } == true
    }

    /**
     * Increments the value of the `pageBannerUpdateObservable` to trigger an update for the page banner.
     * This function is typically invoked when the banner's content needs to be refreshed.
     */
    fun updateBanner() {
        pageBannerUpdateObservable.value++
    }

    /**
     * Builds and returns an off-canvas filter view within the current container.
     *
     * This method is designed to create an off-canvas filter component which can
     * be used for applying filters within the UI. If no specific filter view is
     * defined, it returns null.
     *
     * @return An instance of Offcanvas representing the filter view, or null if none is defined.
     */
    open fun Container.buildOffCanvasFilterView(): Offcanvas? = null
}
