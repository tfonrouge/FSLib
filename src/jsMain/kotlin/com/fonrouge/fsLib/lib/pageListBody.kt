package com.fonrouge.fsLib.lib

import com.fonrouge.fsLib.config.ConfigViewList
import com.fonrouge.fsLib.view.ViewList
import io.kvision.core.Container

/**
 * Allows to insert the [pageListBody] from and external [ViewList]
 *
 * @param configViewList the [ConfigViewList] of the external viewList to embed
 */
fun <V1 : ViewList<*, *, *, *, *>> Container.pageListBody(
    configViewList: ConfigViewList<*, *, *, V1, *, *, *>,
    urlParams: UrlParams? = null,
    init: (V1.() -> Unit)? = null
) {
    val viewList: V1 = configViewList.newViewInstance(urlParams = urlParams)
    with(viewList) {
        pageListBody()
    }
    init?.invoke(viewList)
}

/**
 * Allows to insert the [pageListBody] from and external [ViewList]
 *
 * @param viewList the external [ViewList] to embed
 */
fun <V1 : ViewList<*, *, *, *, *>> Container.pageListBody(
    viewList: V1,
    init: (V1.() -> Unit)? = null
) {
    with(viewList) {
        pageListBody()
    }
    init?.invoke(viewList)
}
