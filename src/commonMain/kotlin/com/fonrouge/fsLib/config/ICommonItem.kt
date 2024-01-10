package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import kotlinx.serialization.KSerializer

abstract class ICommonItem<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
    val labelIdFunc: ((T?) -> String)? = { it?._id?.toString() ?: "<no-item>" },
    val itemSerializer: KSerializer<T>,
    val idSerializer: KSerializer<ID>,
    label: String,
    apiFilterSerializer: KSerializer<FILT>
) : ICommonContainer<FILT>(
    label = label,
    apiFilterSerializer = apiFilterSerializer
)
