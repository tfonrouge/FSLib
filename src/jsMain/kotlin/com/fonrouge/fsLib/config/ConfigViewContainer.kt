package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.view.ViewDataContainer
import kotlin.reflect.KClass

abstract class ConfigViewContainer<CV : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, V : ViewDataContainer<CV,T,ID, FILT>, FILT : IApiFilter>(
    override val commonView: CV,
    viewFunc: KClass<out V>,
    baseUrl: String? = null,
) : ConfigView<CV, V, FILT>(
    viewFunc = viewFunc,
    commonView = commonView,
    _baseUrl = baseUrl,
)
