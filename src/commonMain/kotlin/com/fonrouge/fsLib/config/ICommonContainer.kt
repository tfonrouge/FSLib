package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.IApiFilter
import kotlinx.serialization.KSerializer

abstract class ICommonContainer<FILT : IApiFilter>(
    label: String,
    apiFilterSerializer: KSerializer<FILT>
) : ICommon<FILT>(
    label = label,
    apiFilterSerializer = apiFilterSerializer
)
