package com.fonrouge.fsLib.lib

import com.fonrouge.fsLib.model.state.ISimpleState
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions

/**
 * https://github.com/CodeSeven/toastr
 */
@Suppress("unused")
fun ISimpleState.toast() {
    val options = ToastOptions()
    when (isOk) {
        true -> Toast.success(message = msgOk ?: "Ok", options = options)
        false -> Toast.warning(message = msgError ?: "Unkown error", options = options)
    }
}
