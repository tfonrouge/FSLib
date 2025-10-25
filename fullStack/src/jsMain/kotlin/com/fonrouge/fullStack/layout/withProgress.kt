package com.fonrouge.fullStack.layout

import io.kvision.html.span
import io.kvision.modal.Modal
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.w3c.dom.Element
import kotlin.time.Duration.Companion.milliseconds

/**
 * Executes a suspending block of code while displaying a modal progress indicator.
 *
 * This method displays a modal dialog with a title and optional descriptive text while the specified
 * suspending block is executed. The modal will be automatically hidden when the block's execution is completed
 * or if an exception occurs.
 *
 * @param title The title of the modal dialog. Defaults to "one moment ...".
 * @param text The optional descriptive text to display within the modal dialog. Can be null.
 * @param block The suspending block of code to execute while the modal is displayed.
 */
@Suppress("unused")
fun CoroutineScope.withProgress(
    title: String = "one moment ...",
    text: String? = null,
    block: suspend CoroutineScope.() -> Unit
) {
    val modal = Modal(caption = title, closeButton = false, centered = true, escape = false) {
        id = "modal"
        text?.let { span(it, rich = true) }
    }
    modal.show()
    launch {
        try {
            block()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            var x = 0
            while (x < 10) {
                x++
                delay(100.milliseconds)
                if (window.getComputedStyle(modal.getElement() as Element).opacity == "1") {
                    delay(100.milliseconds)
                    break
                }
            }
            // TODO: Find out a better way to wait for the modal to disappear
            modal.hide()
        }
    }
}