package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.config.ConfigView
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.lib.iconCrud
import io.kvision.core.BsBgColor
import io.kvision.core.Container
import io.kvision.html.*
import io.kvision.navbar.Navbar
import io.kvision.navbar.nav
import io.kvision.navbar.navbar
import io.kvision.state.ObservableValue
import io.kvision.state.bind
import io.kvision.utils.em
import io.kvision.utils.px

abstract class View(
    open val configView: ConfigView<*>,
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
    abstract var urlParams: UrlParams?
    private val pageBannerUpdateObservable = ObservableValue(0)

    /**
     * Allows to insert the whole view to the current DSL container
     */
    fun Container.add(view: View): Container {
        view.apply {
            displayPage()
        }
        return this
    }

    /**
     * Allows to describe a display that will be showed next to the view link banner
     * it can be triggered with [updateBanner] function
     */
    open fun Container.bannerLegend() {

    }

    abstract fun Container.displayPage()

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
}
