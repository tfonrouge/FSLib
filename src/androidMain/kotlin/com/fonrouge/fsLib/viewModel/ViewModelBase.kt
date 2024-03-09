package com.fonrouge.fsLib.viewModel

import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.fonrouge.fsLib.model.state.ISimpleState
import com.fonrouge.fsLib.model.state.SimpleState
import com.fonrouge.fsLib.model.state.State
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
        simpleState: ISimpleState,
        navHostController: NavHostController?,
        onDismissRequest: () -> Unit = {},
    ) {
        val type: SimpleStateAlert.Type = when (simpleState.state) {
            State.Ok -> SimpleStateAlert.Type.Info(
                onAccept = { navHostController?.navigateUp() }
            )

            State.Warn -> SimpleStateAlert.Type.Warn(
                canRetry = true,
                onCancel = { navHostController?.navigateUp() }
            )

            State.Error -> SimpleStateAlert.Type.Error(
                canRetry = false,
                onAccept = { navHostController?.navigateUp() }
            )
        }
        _alertState.value =
            SimpleStateAlert(
                simpleState = simpleState,
                type = type,
                onDismissRequest = onDismissRequest,
            )
    }
}

data class SimpleStateAlert(
    val simpleState: ISimpleState,
    val type: Type = Type.Info(),
    val onDismissRequest: (() -> Unit) = {},
) {
    sealed class Type(
        open val canRetry: Boolean = false,
        open val onAccept: (() -> Unit)? = null,
        open val onCancel: (() -> Unit)? = null,

        ) {
        data class Confirm(
            override val onAccept: (() -> Unit)? = null
        ) : Type()

        data class Info(
            override val onAccept: (() -> Unit)? = null
        ) : Type()

        data class Warn(
            override val canRetry: Boolean,
            override val onAccept: (() -> Unit)? = null,
            override val onCancel: (() -> Unit)? = null,
        ) : Type()

        data class Error(
            override val canRetry: Boolean,
            override val onAccept: (() -> Unit)? = null,
            override val onCancel: (() -> Unit)? = null,
        ) : Type()
    }
}
