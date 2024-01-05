package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState

abstract class ICommonViewItem<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
    label: String,
    var labelIdFunc: ((T?) -> String?)? = { it?._id?.toString() ?: "<no-item>" },
) : ICommonViewContainer<FILT>(
    label = label,
) {
    var apiItem: ApiItem<T, ID, FILT>? = null
    var itemState: ItemState<T>? = null
}
