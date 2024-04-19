package com.fonrouge.fsLib.lib

import com.fonrouge.fsLib.config.ConfigView
import com.fonrouge.fsLib.config.ICommon
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.view.View

/**
 * Builds an url with an [apiFilter] parameter value
 *
 * @param configView - The [ConfigView] of the [View] to go
 */
fun <FILT : IApiFilter> urlApiFilter(
    configView: ConfigView<out ICommon<FILT>, *, FILT>,
    apiFilter: FILT = configView.commonContainer.apiFilterInstance(),
): String {
    val params = mutableListOf<Pair<String, String>>()
    configView.apiFilterParam(apiFilter).let { params.add(it) }
    return configView.urlWithParams(*params.toTypedArray())
}
