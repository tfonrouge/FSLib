package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import kotlinx.serialization.KSerializer

abstract class ICommonList<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
    label: String,
    val itemSerializer: KSerializer<T>,
    val idSerializer: KSerializer<ID>,
    apiFilterSerializer: KSerializer<FILT>
) : ICommonContainer<FILT>(
    label = label,
    apiFilterSerializer = apiFilterSerializer
)
