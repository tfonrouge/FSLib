package com.fonrouge.fullStack.tabulator

import io.kvision.core.onEventLaunch
import io.kvision.tabulator.Tabulator

/**
 * Registers an event handler to be executed when the "tableBuiltTabulator" event is triggered.
 *
 * @param block A lambda function to handle the event. The receiver is the Tabulator instance, and the event object is passed as a parameter.
 */
@Suppress("unused")
fun Tabulator<*>.onTableBuiltEvent(block: Tabulator<*>.(e: dynamic) -> Unit) {
    onEventLaunch("tableBuiltTabulator", handler = block)
}
