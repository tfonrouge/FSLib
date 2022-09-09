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
) {
    var caption: String? = null
    var linkBanner: Link? = null
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

    fun getCaption(): String {
        return caption ?: when (urlParams?.crudAction) {
            CrudAction.Create -> "[${CrudAction.Create}] "
            CrudAction.Update -> "[${CrudAction.Update}] "
            else -> ""
        }.let { "$it${configView.label}: ${label()}" }
    }

    open fun label(): String {
        return ""
    }

    open fun onBeforeDisplayPage(container: Container) {}

    open fun onBeforeDispose() {}

    fun Container.pageBanner(onUpdatePageBannerLink: ((Link) -> Unit)? = null) {
        flexPanel(
            FlexDirection.ROW,
            FlexWrap.NOWRAP,
            JustifyContent.FLEXSTART,
            AlignItems.BASELINE,
            className = "container-fluid mainBanner"
        ) {
            linkBanner = link(label = getCaption(), url = navigoUrlWithParams, className = "navbar-brand mainBanner") {
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
