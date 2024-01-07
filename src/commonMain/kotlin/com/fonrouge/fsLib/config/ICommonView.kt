package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.IApiFilter
import kotlinx.serialization.KSerializer

// TODO: integrate with IApiService
abstract class ICommonView<FILT : IApiFilter>(
    val label: String,
    val apiFilterSerializer: KSerializer<FILT>? = null,
) {
    val name: String get() = this::class.simpleName?.removePrefix("Common") ?: "?"
    open fun route(): String = name
}
