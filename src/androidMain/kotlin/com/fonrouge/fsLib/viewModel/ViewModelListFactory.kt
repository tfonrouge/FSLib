package com.fonrouge.fsLib.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.apiData.ApiList
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ListState
import kotlin.reflect.KSuspendFunction1

@Suppress("unused")
class ViewModelListFactory<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
    private val cc: CC,
    private val listStateFun: KSuspendFunction1<ApiList<FILT>, ListState<T>>
) : ViewModelProvider.NewInstanceFactory() {
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
        return ViewModelList<CC, T, ID, FILT>(commonContainer = cc, listStateFun = listStateFun) as VM
    }
}
