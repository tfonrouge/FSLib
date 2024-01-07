package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

abstract class ICommonViewItem<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
    val labelIdFunc: ((T?) -> String)? = { it?._id?.toString() ?: "<no-item>" },
    val itemSerializer: KSerializer<T>,
    val idSerializer: KSerializer<ID>,
    label: String,
    apiFilterSerializer: KSerializer<FILT>
) : ICommonViewContainer<FILT>(
    label = label,
    apiFilterSerializer = apiFilterSerializer
) {
    var apiItem: ApiItem<T, ID, FILT>? = null
    var itemState: ItemState<T>? = null
    var onQueryFail: (ICommonViewItem<T, ID, FILT>.() -> Unit)? = null
    var onQuerySuccess: (ICommonViewItem<T, ID, FILT>.() -> Unit)? = null
    var onActionFail: (ICommonViewItem<T, ID, FILT>.() -> Unit)? = null
    var onActionSuccess: (ICommonViewItem<T, ID, FILT>.() -> Unit)? = null
}
