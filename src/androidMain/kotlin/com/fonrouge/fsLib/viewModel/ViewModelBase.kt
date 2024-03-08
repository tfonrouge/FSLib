package com.fonrouge.fsLib.viewModel

import androidx.lifecycle.ViewModel
import com.fonrouge.fsLib.model.state.ItemState
import com.fonrouge.fsLib.model.state.SimpleState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class ViewModelBase : ViewModel() {
    private val _snackBarStatus = MutableStateFlow<SimpleState?>(null)
    val snackBarStatus = _snackBarStatus.asStateFlow()
    private val _alertState = MutableStateFlow<SimpleStateAlert?>(null)
    var alertState = _alertState.asStateFlow()

    fun pushSimpleState(simpleState: SimpleState?) {
        _snackBarStatus.value = simpleState
    }

    fun clearAlert() {
        _alertState.value = null
    }

    fun pushAlert(
        simpleState: SimpleState,
        canRetry: Boolean = false,
        onDismissRequest: () -> Unit = {},
        onCancel: () -> Unit = {},
        onRetry: (() -> Unit)? = null,
        onAccept: () -> Unit = {}
    ) {
        _alertState.value =
            SimpleStateAlert(
                simpleState = simpleState,
                canRetry = canRetry,
                onDismissRequest = onDismissRequest,
                onCancel = onCancel,
                onRetry = onRetry,
                onAccept = onAccept
            )
    }

    fun pushAlert(
        itemState: ItemState<*>,
        canRetry: Boolean = false,
        onDismissRequest: () -> Unit = {},
        onCancel: () -> Unit = {},
        onRetry: (() -> Unit)? = null,
        onAccept: () -> Unit = {}
    ) {
        pushAlert(
            simpleState = itemState.asSimpleState,
            canRetry = canRetry,
            onDismissRequest = onDismissRequest,
            onCancel = onCancel,
            onRetry = onRetry,
            onAccept = onAccept
        )
    }
}

data class SimpleStateAlert(
    val simpleState: SimpleState,
    val canRetry: Boolean = false,
    val onDismissRequest: (() -> Unit) = {},
    val onCancel: (() -> Unit) = {},
    val onRetry: (() -> Unit)? = null,
    val onAccept: (() -> Unit) = {},
)
