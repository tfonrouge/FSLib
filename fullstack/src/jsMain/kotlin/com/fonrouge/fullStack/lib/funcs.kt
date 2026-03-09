package com.fonrouge.fullStack.lib

import io.kvision.pace.Pace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

var progressCount = 0

/**
 * Launches a coroutine that executes the given suspendable block while showing a progress indicator.
 * The progress indicator will remain visible until all progress-indicating coroutines complete.
 * This method ensures that the progress indicator is hidden even if an exception occurs in the block.
 *
 * @param block The suspendable code block to be executed within the coroutine.
 * @return A Job representing the coroutine, which can be used to cancel or monitor its execution.
 */
@Suppress("unused")
fun CoroutineScope.withPace(block: suspend () -> Unit): Job {
    Pace.show()
    progressCount++
    return launch {
        try {
            block()
            progressCount--
        } catch (e: Exception) {
            progressCount--
            throw e
        } finally {
            if (progressCount <= 0) Pace.hide()
        }
    }
}
