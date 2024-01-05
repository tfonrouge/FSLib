package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.IApiFilter

abstract class CommonView<FILT : IApiFilter>(
    val label: String,
) {
    val name: String get() = this::class.simpleName ?: "?"
}
