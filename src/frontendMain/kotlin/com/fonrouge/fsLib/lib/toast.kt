package com.fonrouge.fsLib.lib

import com.fonrouge.fsLib.model.ISimpleResponse
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions

@Suppress("unused")
fun ISimpleResponse.toast() {
    val options = ToastOptions()
    when (isOk) {
        true -> Toast.success(message = msgOk ?: "Ok", options = options)
        false -> Toast.warning(message = msgError ?: "Unkown error", options = options)
    }
}
