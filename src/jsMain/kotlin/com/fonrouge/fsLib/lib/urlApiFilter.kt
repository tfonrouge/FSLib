package com.fonrouge.fsLib.lib

import com.fonrouge.fsLib.config.ConfigView
import com.fonrouge.fsLib.model.apiData.IApiFilter

/**
 * Builds an url with an [apiFilter] parameter value
 *
 * @param configView - The [ConfigView] of the [View] to go
 */
fun <FILT : IApiFilter> urlApiFilter(
    configView: ConfigView<*, *, FILT>,
    apiFilter: FILT,
): String {
    val params = mutableListOf<Pair<String, String>>()
    configView.apiFilterParam(apiFilter).let { params.add(it) }
    return configView.urlWithParams(*params.toTypedArray())
}
