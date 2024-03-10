package com.fonrouge.fsLib.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc

@Suppress("unused")
class ViewModelListFactory<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
    private val cc: CC
) : ViewModelProvider.NewInstanceFactory() {
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
        return ViewModelList<CC, T, ID, FILT>(cc) as VM
    }
}
