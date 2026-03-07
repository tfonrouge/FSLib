package com.fonrouge.fullStack.layout

import com.fonrouge.fullStack.view.AppScope
import kotlinx.coroutines.CoroutineScope

/**
 * Launches a process with a modal dialog and executes a suspending block of code.
 *
 * Displays a modal dialog with an optional title and a customizable message while executing the provided
 * suspending block. Once the block completes, the modal is hidden.
 *
 * Delegates to [CoroutineScope.withProgress] using [AppScope] as the scope.
 *
 * @param title An optional title for the modal dialog. Defaults to `null`.
 * @param text A message to display in the modal dialog. Defaults to "one moment please...".
 * @param block A suspending lambda to execute while the modal is displayed.
 */
@Suppress("unused")
fun launchProcess(
    title: String? = null,
    text: String = "one moment please...",
    block: suspend CoroutineScope.() -> Unit
) {
    AppScope.withProgress(title = title, text = text, block = block)
}
