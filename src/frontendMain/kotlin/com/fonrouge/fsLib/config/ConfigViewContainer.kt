package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.base.BaseModel
import com.fonrouge.fsLib.view.ViewDataContainer

abstract class ConfigViewContainer<T : BaseModel<*>, V : ViewDataContainer<*>>(
    name: String,
    label: String,
    baseUrlSuffix: String,
    viewFunc: ((UrlParams?) -> V),
) : ConfigView<V>(
    name = name,
    label = label,
    baseUrlPrefix = "data",
    baseUrlSuffix = baseUrlSuffix,
    viewFunc = viewFunc,
)
