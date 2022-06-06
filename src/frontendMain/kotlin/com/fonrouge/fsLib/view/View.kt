package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.config.ConfigView
import com.fonrouge.fsLib.lib.ActionParam
import com.fonrouge.fsLib.lib.UrlParams
import io.kvision.core.*
import io.kvision.html.Link
import io.kvision.html.link
import io.kvision.modal.Modal
import io.kvision.modal.ModalSize
import io.kvision.panel.flexPanel
import kotlinx.serialization.json.JsonObject

abstract class View(
    open val configView: ConfigView<*>,
    val editable: Boolean = true,
    val icon: String? = null,
    val restUrlParams: UrlParams? = null,
    var matchFilterParam: JsonObject? = null,
    var sortParam: JsonObject? = null,
    var upsertData: JsonObject? = null,
    val modal: Boolean = false,
) {

    var objId: Int = -1

    open val repeatUpdateView: Boolean? = null
    var repeatUpdateSecsInterval = 5
    abstract var urlParams: UrlParams?

    var container: Container? = null

//    val eViewUrl: String = navigoPrefix + configView.url

    var pageBannerLink: Link? = null

    var onUpdatePageBannerLink: ((Link) -> Unit)? = null

    val navigoUrlWithParams: String
        get() {
            return configView.navigoUrl + if (urlParams != null) urlParams else ""
        }

    val urlWithParams: String
        get() {
            return configView.url + if (urlParams != null) urlParams else ""
        }

    var caption: String? = null

    fun getCaption(): String {
        return caption ?: when (urlParams?.action) {
            ActionParam.Insert -> "[${ActionParam.Insert}] "
            ActionParam.Update -> "[${ActionParam.Update}] "
            else -> ""
        }.let {
            it + configView.label +
                    if (this@View is ViewItem<*, *>) {
                        getName().let { it1 ->
                            if (it1 == null) "" else ": $it1"
                        }
                    } else ""
        }
    }

    fun updateMainBannerLink(text: String, url: String) {
        pageBannerLink?.label = "${configView.label}: $text"
        pageBannerLink?.url = "${configView.navigoUrl}/$url"
    }

    open fun getName(): String? {
        return null
    }

    fun displayModal(
        caption: String? = null, closeButton: Boolean = true,
        size: ModalSize? = null, animation: Boolean = true, centered: Boolean = false,
        scrollable: Boolean = false, escape: Boolean = true,
        className: String? = null,
    ) {
        val modal = Modal(
            caption = caption,
            closeButton = closeButton,
            size = size,
            animation = animation,
            centered = centered,
            scrollable = scrollable,
            escape = escape,
            className = className
        )
        displayPage(modal)
    }

    abstract fun displayPage(container: Container)

    open fun onDisplayPage() {}

    fun Container.pageBanner(onUpdatePageBannerLink: ((Link) -> Unit)? = null) {
        flexPanel(
            FlexDirection.ROW,
            FlexWrap.NOWRAP,
            JustifyContent.FLEXSTART,
            AlignItems.BASELINE,
            className = "container-fluid mainBanner"
        ) {
            link(getCaption(), navigoUrlWithParams, className = "navbar-brand mainBanner") {
                setStyle("color", "white")
            }
            onUpdatePageBannerLink?.let { it ->
//                onUpdatePageBannerLink = it
                pageBannerLink?.let { link -> onUpdatePageBannerLink(link) }
            }
        }
    }
}
