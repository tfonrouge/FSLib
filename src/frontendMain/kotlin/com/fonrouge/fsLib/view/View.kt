package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.config.ConfigView
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.CrudAction
import io.kvision.core.Container
import io.kvision.html.ButtonStyle
import io.kvision.html.Link
import io.kvision.html.button
import io.kvision.html.link
import io.kvision.navbar.nav
import io.kvision.navbar.navbar
import io.kvision.utils.em
import io.kvision.utils.px

abstract class View(
    open val configView: ConfigView<*>,
    var editable: Boolean = true,
    val icon: String? = null,
    open val label: String = configView.label
) {
    private var linkBanner: Link? = null
    val navigoUrlWithParams: String
        get() {
            return configView.url + if (urlParams != null) urlParams else ""
        }
    var pageBannerLink: Link? = null
    var onUpdatePageBannerLink: ((Link) -> Unit)? = null

    /**
     * Enable/disable periodic refresh interval of view
     */
    open val periodicUpdateDataView: Boolean? = null
    var periodicUpdateViewInterval = 5
    abstract var urlParams: UrlParams?
    abstract fun Container.displayPage()

    open fun onBeforeDisplayPage(container: Container) {}

    open fun onBeforeDispose() {}

    open var labelBanner: String?
        get() {
            return linkBanner?.label
        }
        set(value) {
            if (value != null) {
                linkBanner?.label = value
            }
        }

    fun iconCrud(crudAction: CrudAction? = null): String? {
        return when (crudAction ?: urlParams?.crudAction) {
            CrudAction.Create -> "fas fa-plus"
            CrudAction.Read -> "fas fa-eye"
            CrudAction.Update -> "fas fa-edit"
            CrudAction.Delete -> "fas fa-trash-alt"
            null -> null
        }
    }

    fun Container.pageBanner(onUpdatePageBannerLink: ((Link) -> Unit)? = null) {
        navbar(label = label) {
            linkBanner = link(
                label = label ?: "",
                url = navigoUrlWithParams,
                className = "navbar-brand mainBanner",
                icon = iconCrud()
            ) {
                setStyle("color", "white")
            }
            nav(rightAlign = true) {
                if (urlParams?.actionUpsert == true) {
                    button(text = "Cancel", icon = "fas fa-xmark", style = ButtonStyle.OUTLINEDANGER)
                    button(text = "Accept", icon = "fas fa-check", style = ButtonStyle.OUTLINESUCCESS) {
                        marginLeft = 20.px
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

    fun updateMainBannerLink(text: String, url: String) {
        pageBannerLink?.label = "${configView.label}: $text"
        pageBannerLink?.url = "${configView.url}/$url"
    }
}
