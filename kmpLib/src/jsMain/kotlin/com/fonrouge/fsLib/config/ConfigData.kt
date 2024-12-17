package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.IApiFilter

abstract class ConfigData<CC : ICommon<FILT>, FILT : IApiFilter<*>>(
    open val commonContainer: CC
)

fun <CC : ICommon<FILT>, FILT : IApiFilter<*>> configData(
    commonContainer: CC,
): ConfigData<CC, FILT> = object : ConfigData<CC, FILT>(commonContainer) {}
