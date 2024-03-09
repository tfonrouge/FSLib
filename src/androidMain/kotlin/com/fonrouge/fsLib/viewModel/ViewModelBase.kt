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
    private val _stateAlert = MutableStateFlow<StateAlert?>(null)
    var stateAlert = _stateAlert.asStateFlow()
    private val _confirmAlert = MutableStateFlow<ConfirmAlert?>(null)
    var confirmAlert = _confirmAlert.asStateFlow()

    fun pushSimpleState(simpleState: SimpleState?) {
        _snackBarStatus.value = simpleState
    }

    fun clearStateAlert() {
        _stateAlert.value = null
    }

    fun clearConfirmAlert() {
        _confirmAlert.value = null
    }

    fun pushConfirmAlert(
        confirmText: String,
        onNo: (() -> Unit)? = null,
        onDismissRequest: () -> Unit = {},
        onConfirm: () -> Unit
    ) {
        _confirmAlert.value = ConfirmAlert(
            type = ConfirmAlert.Type.YesNoConfirm(
                onConfirm = onConfirm,
                onNo = onNo
            ),
            confirmText = confirmText,
            onDismissRequest = onDismissRequest
        )
    }

    fun pushConfirmCancelAlert(
        confirmText: String,
        onCancel: (() -> Unit)? = null,
        onDismissRequest: () -> Unit = {},
        onConfirm: () -> Unit
    ) {
        _confirmAlert.value = ConfirmAlert(
            type = ConfirmAlert.Type.YesCancelConfirm(
                onConfirm = onConfirm,
                onCancel = onCancel
            ),
            confirmText = confirmText,
            onDismissRequest = onDismissRequest
        )
    }

    fun pushStateAlert(
        simpleState: ISimpleState,
        navHostController: NavHostController?,
        onDismissRequest: () -> Unit = {},
    ) {
        val type: StateAlert.Type = when (simpleState.state) {
            State.Ok -> StateAlert.Type.Info(
                onAccept = { navHostController?.navigateUp() }
            )

            State.Warn -> StateAlert.Type.Warn(
                canRetry = true,
                onCancel = { navHostController?.navigateUp() }
            )

            State.Error -> StateAlert.Type.Error(
                canRetry = false,
                onAccept = { navHostController?.navigateUp() }
            )
        }
        _stateAlert.value =
            StateAlert(
                simpleState = simpleState,
                type = type,
                onDismissRequest = onDismissRequest,
            )
    }
}

data class StateAlert(
    val simpleState: ISimpleState,
    val type: Type = Type.Info(),
    val onDismissRequest: (() -> Unit) = {},
) {
    sealed class Type(
        open val canRetry: Boolean = false,
        open val onAccept: (() -> Unit)? = null,
        open val onCancel: (() -> Unit)? = null,
    ) {
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

data class ConfirmAlert(
    val type: Type,
    val confirmText: String = "?",
    val onDismissRequest: (() -> Unit) = {},
) {
    sealed class Type(
        open val onConfirm: (() -> Unit),
    ) {
        data class YesNoConfirm(
            override val onConfirm: (() -> Unit),
            val onNo: (() -> Unit)? = null,
        ) : Type(onConfirm)

        class YesCancelConfirm(
            override val onConfirm: (() -> Unit),
            val onCancel: (() -> Unit)? = null,
        ) : Type(onConfirm)
    }
}
