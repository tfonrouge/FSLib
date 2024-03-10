package com.fonrouge.fsLib.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import kotlin.reflect.KSuspendFunction1

@Suppress("unused")
class ViewModelItemFactory<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
    private val commonContainer: CC,
    private val itemStateFun: KSuspendFunction1<ApiItem<T, ID, FILT>, ItemState<T>>
) : ViewModelProvider.NewInstanceFactory() {
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
        return ViewModelItem(
            commonContainer = commonContainer,
            itemStateFun = itemStateFun
        ) as VM
    }
}
