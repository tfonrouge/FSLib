package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.ApiList
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ListState

abstract class CommonViewList<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
    label: String,
) : CommonViewContainer<FILT>(
    label = label,
) {
    var apiList: ApiList<FILT>? = null
    var listState: ListState<T>? = null
}
