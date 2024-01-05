package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.IApiFilter

abstract class ICommonView<FILT : IApiFilter>(
    val label: String,
) {
    val name: String get() = this::class.simpleName?.removePrefix("Common") ?: "?"
}
