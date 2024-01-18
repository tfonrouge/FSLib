package com.fonrouge.fsLib.viewModel

import androidx.lifecycle.ViewModel
import com.fonrouge.fsLib.model.state.SimpleState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class ViewModelBase : ViewModel() {
    private val _snackBarStatus = MutableStateFlow<SimpleState?>(null)
    val snackBarStatus = _snackBarStatus.asStateFlow()

    fun pushSimpleState(simpleState: SimpleState?) {
        _snackBarStatus.value = simpleState
    }
}
