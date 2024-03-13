package com.fonrouge.fsLib.viewModel

import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import kotlin.reflect.KSuspendFunction1

@Suppress("unused")
abstract class ViewModelItem<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
    final override val commonContainer: CC,
    final override val itemStateFun: KSuspendFunction1<ApiItem<T, ID, FILT>, ItemState<T>>
) : ViewModelContainer<CC, T, ID, FILT>() {
    override var apiItem: ApiItem<T, ID, FILT> = commonContainer.apiItem()
    override var apiFilter: FILT = commonContainer.apiFilterInstance()
}
