package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.IApiFilter
import kotlinx.serialization.KSerializer

abstract class ICommonViewContainer<FILT : IApiFilter>(
    label: String,
    apiFilterSerializer: KSerializer<FILT>? = null
) : ICommonView<FILT>(
    label = label,
    apiFilterSerializer = apiFilterSerializer
)
