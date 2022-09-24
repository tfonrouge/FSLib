package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.base.BaseModel
import com.fonrouge.fsLib.view.ViewDataContainer
import kotlin.reflect.KClass

abstract class ConfigViewContainer<T : BaseModel<*>, V : ViewDataContainer<*>>(
    name: String,
    label: String,
    viewFunc: KClass<V>,
    baseUrl: String,
) : ConfigView<V>(
    name = name,
    label = label,
    viewFunc = viewFunc,
    baseUrl = baseUrl,
)
