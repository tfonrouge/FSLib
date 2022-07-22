package com.fonrouge.fsLib.lib

import io.kvision.pace.Pace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

var progressCount = 0

fun CoroutineScope.withProgress(block: suspend () -> Unit): Job {
    Pace.show()
    progressCount++
    return launch {
        try {
            block()
            progressCount--
            if (progressCount <= 0) Pace.hide()
        } catch (e: Exception) {
            progressCount--
            if (progressCount <= 0) Pace.hide()
            throw e
        }
    }
}
