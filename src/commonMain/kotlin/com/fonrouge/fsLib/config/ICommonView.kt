package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.IApiFilter

// TODO: integrate with IApiService
abstract class ICommonView<FILT : IApiFilter>(
    val label: String,
) {
    val name: String get() = this::class.simpleName?.removePrefix("Common") ?: "?"
}
