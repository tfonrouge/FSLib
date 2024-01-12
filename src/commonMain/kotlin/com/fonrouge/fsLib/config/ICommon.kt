package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.IApiFilter
import kotlinx.serialization.KSerializer

// TODO: integrate with IApiService
abstract class ICommon<FILT : IApiFilter>(
    var label: String = "",
    val apiFilterSerializer: KSerializer<FILT>,
) {
    val name: String get() = this::class.simpleName?.removePrefix("Common") ?: "?"
    open fun route(): String = name
}
