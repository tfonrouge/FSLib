package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.config.ConfigView
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.CrudAction
import io.kvision.core.*
import io.kvision.html.Link
import io.kvision.html.link
import io.kvision.panel.flexPanel
import io.kvision.utils.em

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
    open val repeatUpdateView: Boolean? = null
    var repeatUpdateSecsInterval = 5
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
        flexPanel(
            FlexDirection.ROW,
            FlexWrap.NOWRAP,
            JustifyContent.FLEXSTART,
            AlignItems.BASELINE,
            className = "container-fluid mainBanner"
        ) {
            linkBanner = link(
                label = label,
                url = navigoUrlWithParams,
                className = "navbar-brand mainBanner",
                icon = iconCrud()
            ) {
                setStyle("color", "white")
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
