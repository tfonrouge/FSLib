package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.view.ViewDataContainer
import kotlin.reflect.KClass

abstract class ConfigViewContainer<CC : ICommonContainer<T, ID, FILT, *>, T : BaseDoc<ID>, ID : Any, V : ViewDataContainer<CC, T, ID, FILT>, FILT : IApiFilter>(
    override val commonContainer: CC,
    viewFunc: KClass<out V>,
    baseUrl: String? = null,
) : ConfigView<CC, V, FILT>(
    viewFunc = viewFunc,
    commonContainer = commonContainer,
    _baseUrl = baseUrl,
)
