package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.lib.UrlParams
import io.kvision.core.Container

open class ViewHomeBase(
    override var urlParams: UrlParams? = null
) : View(
    configView = null,
//    actionPage = { view -> IfceWebAction.HomePage(view as ViewHomeBase) }
) {
    override fun displayPage(container: Container) {
//        TODO("Not yet implemented")
    }
}
