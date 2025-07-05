package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.common.ICommon
import com.fonrouge.fsLib.common.ICommonContainer
import com.fonrouge.fsLib.config.ConfigView
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.lib.iconCrud
import com.fonrouge.fsLib.lib.toEncodedUrlString
import com.fonrouge.fsLib.model.apiData.CrudTask
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.view.KVWebManager.frontEndAppName
import io.kvision.core.*
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
import web.dom.document

/**
 * Represents an abstract base class for creating views with configurable display elements.
 *
 * This class provides functionality to manage URL parameters, display labels,
 * periodic updates, API filters, banners, and navigation buttons.
 *
 * @param CC The type of the common container that implements ICommon.
 * @param FILT The type of the API filter that implements IApiFilter.
 * @property configView The configuration view instance providing necessary configurations.
 */
abstract class View<out CC : ICommon<FILT>, FILT : IApiFilter<*>>(
    open val configView: ConfigView<CC, *, FILT>,
) {
    /**
     * Represents a set of URL parameters associated with the `View` class.
     *
     * This variable provides access to the `UrlParams` object, enabling the retrieval, management,
     * and manipulation of query parameters within the associated view. The `UrlParams` object allows
     * operations such as obtaining specific parameters, adding or updating parameters, and performing
     * CRUD-related tasks (e.g., creating or updating an entry).
     *
     * It may also include logic for interpreting and handling parameters to determine specific actions
     * or states related to the view, such as CRUD operations or entity identification.
     *
     * Nullable, as URL parameters might not always be initialized or required for a given view.
     */
    var urlParams: UrlParams = UrlParams()

    /**
     * A computed property that determines whether the current CRUD task is either a creation or an update operation.
     *
     * This property evaluates to `true` if the assigned `crudTask` value is either `CrudTask.Create` or `CrudTask.Update`.
     * It is specifically used to identify scenarios where modifications (creation or update) are being performed.
     */
    val actionUpsert: Boolean
        get() {
            return crudTask in listOf(CrudTask.Create, CrudTask.Update)
        }

    /**
     * A nullable property representing the current CRUD task for the view.
     *
     * This property lazily resolves a `CrudTask` value based on the `action` parameter
     * obtained from `urlParams`. If the property is accessed for the first time and its value
     * is null, it attempts to find a corresponding entry in the `CrudTask` enum class using
     * the `action` parameter value and assigns it to the property.
     *
     * @see CrudTask
     */
    var crudTask: CrudTask? = null
        get() {
            if (field == null) {
                field = CrudTask.entries.find { it.name == urlParams.params["action"] }
            }
            return field
        }

    open val label: String get() = configView.label
    var mainView: Boolean = false
    var navButtonCancel: Button? = null
    var navButtonAccept: Button? = null
    var navButtonBack: Button? = null
    val navigoUrlWithParams: String
        get() {
            return configView.url + urlParams
        }

    /**
     * Set to true to don't display the [pageBanner]
     */
    var noPageBanner = false

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
     * Observable representation of the API filter used by the view.
     *
     * This property lazily initializes an observable value for the API filter, allowing
     * the system to react to changes in the filter state dynamically. It is tied to the
     * `apiFilterInstance` method for the default filter configuration.
     *
     * The observable value is primarily used within the view to monitor and respond to
     * updates to the filtering logic, which may impact the displayed data or UI components.
     */
    val apiFilterObservable: ObservableValue<FILT> by lazy {
        ObservableValue(configView.commonContainer.apiFilterInstance())
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

    val apiFilterFromUrl: FILT?
        get() = urlParams.pullUrlParam(
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
     * Adds a page list body to the container.
     *
     * This method integrates the `pageListBody` of the provided `viewList` into the container,
     * allowing for the display of a list-based UI structure. An optional initialization
     * block can be applied to further configure the `viewList`.
     *
     * @param viewList The view list whose page list body will be added to the container.
     * @param init An optional initialization block to configure the `viewList`. Defaults to null.
     */
    @Suppress("unused")
    fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<MID>, MID : Any> Container.addPageListBody(
        viewList: ViewList<CC, T, ID, FILT, MID>,
        init: (ViewList<CC, T, ID, FILT, MID>.() -> Unit)? = null,
    ) {
        with(viewList) {
            pageListBody()
        }
        init?.invoke(viewList)
    }

    /**
     * Adds a view to the container and renders its main content.
     *
     * This method invokes the `displayPage` function of the provided view, which
     * is responsible for rendering the content within the container.
     *
     * @param view The view to be added and displayed within the container.
     * @return The container itself, allowing for method chaining.
     */
    @Suppress("unused")
    fun Container.addView(view: View<*, *>): Container {
        view.apply {
            startDisplayPage()
        }
        return this
    }

    /**
     * Adds a list of views to the container and initializes them.
     *
     * This method invokes the `startDisplayPage` function of the provided view list to prepare
     * the content for display. Optionally, a master view item can be associated with the view
     * list, and an initialization block can be applied to perform additional configuration.
     *
     * @param viewList The view list to be added and displayed within the container.
     * @param masterViewItem An optional master view item associated with the view list. Defaults to null.
     * @param init An optional initialization block to configure the view list. Defaults to null.
     */
    @Suppress("unused")
    fun <FILT : IApiFilter<MID>, MID : Any> Container.addViewList(
        viewList: ViewList<ICommonContainer<*, *, FILT>, *, *, FILT, MID>,
        masterViewItem: ViewItem<ICommonContainer<out BaseDoc<MID>, MID, *>, out BaseDoc<MID>, MID, *>? = null,
        init: ((ViewList<ICommonContainer<*, *, FILT>, *, *, FILT, MID>).() -> Unit)? = null,
    ) {
        viewList.apply { startDisplayPage() }
        masterViewItem?.let {
            viewList.masterViewItem = it
        }
        init?.invoke(viewList)
    }

    /**
     * Converts the current API filter to a URL string and optionally updates the browser's history state.
     *
     * This method serializes the current API filter to a URL parameter, appends it to the base URL,
     * and optionally replaces the browser's history state with the updated URL.
     *
     * @param replaceState A boolean indicating whether the browser's history state should be updated
     *                     with the new URL. If true, the history state is replaced with the modified URL.
     * @return A string representing the encoded URL with the API filter parameter appended.
     */
    fun apiFilterToPageUrl(replaceState: Boolean): String {
        configView.pairParam("apiFilter", configView.commonContainer.apiFilterSerializer, apiFilter)
            .let { pair ->
                urlParams.params[pair.first] = pair.second
            }
        val url = (configView.url + urlParams.toEncodedUrlString()).asDynamic()
        if (replaceState) {
            @Suppress("UNUSED_VARIABLE", "unused")
            val stateObj = "{apiFilter: toUrl}".asDynamic()
            js("""history.replaceState(stateObj,"createToUpdate",url)""")
        }
        return url
    }

    /**
     * Allows describing a display that will be shown next to the view link banner
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
     * Constructs and returns a label for the banner based on the provided API filter.
     * This function is designed to create a string label specific to the given filter configuration.
     *
     * @param apiFilter The API filter instance that will be used to generate the banner's label.
     * @return A string representing the label for the banner.
     */
    open suspend fun labelBanner(apiFilter: FILT): String = label

    /**
     * Initiates the display of a page within the container.
     *
     * The method sets up the necessary hooks and bindings to ensure the page is properly rendered
     * and updated. It begins by attaching a pre-dispose hook using `addBeforeDisposeHook` to call
     * `onBeforeDispose` for cleanup tasks. The `onBeforeDisplayPage` method is called to allow
     * any pre-rendering logic or UI configuration. It then invokes the `displayPage` method
     * to render the main content of the page.
     *
     * A binding to `apiFilterObservable` is established to monitor changes in the API filter.
     * This triggers the `onApiFilterChange` method, which can be used to handle any updates,
     * and `apiFilterToUrl` to update the browser's URL appropriately. After the initial rendering
     * and setup, the `onAfterDisplayPage` method is called to perform any post-rendering actions.
     *
     * @param mainView A boolean indicating whether the current view should be treated as the main view.
     *                 If true, the API filter will be added to the URL parameters upon changes.
     */
    fun Container.startDisplayPage(mainView: Boolean = false) {
        this@View.mainView = mainView
        div {
            addBeforeDisposeHook {
                onBeforeDispose()
            }
            apiFilterInit()?.let { apiFilter = it }
            onBeforeDisplayPage(this@startDisplayPage)
            this@startDisplayPage.displayPage()
            bind(apiFilterObservable) {
                onApiFilterChange()
                apiFilterToPageUrl(mainView)
            }
            onAfterDisplayPage()
        }
    }

    /**
     * This method is called immediately after rendering the page's content.
     *
     * Override this method to execute any post-rendering logic or to update the UI components
     * that depend on the page's content.
     */
    open fun onAfterDisplayPage() {}

    /**
     * Initializes and returns the default or custom API filter for the view.
     *
     * This method can be overridden in subclasses to provide a specific implementation
     * for initializing an API filter. The returned filter is typically used
     * to constrain or define the scope of API requests.
     *
     * @return The initialized API filter of type FILT, or null if no filter is specified.
     */
    open fun apiFilterInit(): FILT? = null

    /**
     * This method is called when there is an update to the API filter.
     *
     * The primary function of this method is to invoke the `updateBanner` method,
     * which refreshes the content of the banner legend tied to the current view.
     *
     * Override this method to implement additional logic that should be executed
     * when the API filter is updated.
     */
    open fun onApiFilterChange() {
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
     * Configures and displays a horizontal page banner within a given container.
     *
     * The banner is created using a light-themed Bootstrap navbar and dynamically updates
     * based on observable state changes. It includes navigation links, buttons, and
     * additional elements for filtering and actions dependent on the view state and user interactions.
     *
     * Features:
     * - A dynamically updating label and URL link based on observable states associated with the current view.
     * - Configurable navigation buttons for actions such as back, cancel, or accept, depending on the CRUD task and context.
     * - Optional edit button display in read-only contexts for updating entities.
     * - Capability to include an off-canvas filter for enhanced filtering functionality, which can be shown upon user interaction.
     *
     * Note: This function is intended to be used within a `Container` and assumes that certain contextual data members are available in the surrounding scope.
     *
     * Known Limitation:
     * - Horizontal scrollability for the banner has not been fully implemented and requires additional development.
     */
    fun Container.pageBanner() {
        /* TODO: find out how make horizontally scrollable */
        navbar(bgColor = BsBgColor.LIGHT).bind(
            observableState = pageBannerUpdateObservable,
            removeChildren = true
        ) {
            bannerLeggendContainer().apply {
                link(
                    label = this@View.label,
                    url = navigoUrlWithParams,
                    className = "navbar-brand",
                    icon = if (this@View is ViewItem<*, *, *, FILT>) iconCrud(crudTask) else null,
                ) {
                    apiFilterObservable.subscribe {
                        url = apiFilterToPageUrl(replaceState = false)
                        AppScope.launch {
                            label = labelBanner(it)
                        }
                    }
                    if (this@View is ViewItem<*, *, *, FILT>) {
                        (this@View as ViewItem<*, *, *, FILT>).itemObservable.subscribe {
                            AppScope.launch {
                                label = labelBanner(apiFilter)
                            }
                        }
                    }
                }
                bannerLegend().apply {
                    offCanvasFilter?.let { offCanvasFilter ->
                        cursor = Cursor.POINTER
                        onClick { offCanvasFilter.show() }
                    }
                }
            }
            nav(rightAlign = true) {
                if (this@View is ViewItem<*, *, *, FILT>) {
                    if (actionUpsert) {
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
                    } else if (crudTask == CrudTask.Read) {
                        displayEditButton()
                    }
                }
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
     * Updates the document's title to match the current value of the `label` property.
     *
     * This method sets the `document.title` to the current value of the `label` field,
     * which serves as the label or title associated with the view. It is typically used
     * to ensure the browser's title reflects the current view's context or purpose.
     */
    fun updateTitle() {
        document.title = "$frontEndAppName - $label"
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
