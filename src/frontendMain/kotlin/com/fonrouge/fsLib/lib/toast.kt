package com.fonrouge.fsLib.lib

import com.fonrouge.fsLib.model.SimpleResponse
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions

@Suppress("unused")
fun SimpleResponse.toast() {
    val options = ToastOptions()
    when (isOk) {
        true -> Toast.success(message = msgOk ?: "Ok", options = options)
        false -> Toast.warning(message = msgError ?: "Unkown error", options = options)
    }
}
