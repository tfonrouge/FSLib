package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.view.ViewDataContainer
import kotlin.reflect.KClass

abstract class ConfigViewContainer<CV : ICommonContainer<FILT>, V : ViewDataContainer<CV, FILT>, FILT : IApiFilter>(
    override val commonView: CV,
    viewFunc: KClass<out V>,
    baseUrl: String? = null,
) : ConfigView<CV, V, FILT>(
    viewFunc = viewFunc,
    commonView = commonView,
    _baseUrl = baseUrl,
)
