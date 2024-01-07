package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.ApiList
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ListState
import kotlinx.serialization.KSerializer

abstract class ICommonViewList<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
    label: String,
    val itemSerializer: KSerializer<T>,
    val idSerializer: KSerializer<ID>,
    apiFilterSerializer: KSerializer<FILT>
) : ICommonViewContainer<FILT>(
    label = label,
    apiFilterSerializer = apiFilterSerializer
) {
    var apiList: ApiList<FILT>? = null
    var listState: ListState<T>? = null
}
