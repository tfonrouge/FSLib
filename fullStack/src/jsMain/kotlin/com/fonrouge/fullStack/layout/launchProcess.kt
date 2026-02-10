package com.fonrouge.fullStack.layout

import com.fonrouge.fullStack.view.AppScope
import io.kvision.core.JustifyContent
import io.kvision.html.span
import io.kvision.modal.Modal
import io.kvision.panel.hPanel
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.w3c.dom.Element
import kotlin.time.Duration.Companion.milliseconds

/**
 * Launches a process with a modal dialog and executes a suspending block of code.
 *
 * This method displays a modal dialog with an optional title and a customizable message while executing the provided
 * suspending block of code. Once the block completes, the modal is hidden. The method ensures proper handling of the
 * modal's visibility and allows smooth user interaction during the process execution. Errors occurring inside the block
 * are caught and logged.
 *
 * @param title An optional title for the modal dialog. Defaults to `null` for no title.
 * @param text A message to display in the modal dialog. Defaults to "one moment please...".
 * @param block A suspending lambda function to execute while the modal is displayed.
 */
@Suppress("unused")
fun launchProcess(
    title: String? = null,
    text: String = "one moment please...",
    block: suspend CoroutineScope.() -> Unit
) {
    val modal = Modal(
        caption = title,
        closeButton = false,
        centered = true,
        escape = false,
        className = "with-progress"
    ) {
        hPanel(justify = JustifyContent.CENTER) {
            span(text, rich = true)
        }
    }
    modal.show()
    AppScope.launch {
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
                    delay(500.milliseconds)
                    break
                }
            }
            // TODO: Find out a better way to wait for the modal to disappear
            modal.hide()
        }
    }
}