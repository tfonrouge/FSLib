package com.fonrouge.fullStack.layout

import io.kvision.i18n.I18n
import io.kvision.i18n.I18nManager
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Pins the help caption building block behind `helpButtons`' late-bound label
 * (blueprints/view-consumer-gaps CONTRACT I-4/I-5, OC-03): the prefix resolves through i18n,
 * and because callers pass the provider's value at open time, a label that changed after FAB
 * construction produces the current caption, never the stale one.
 */
class HelpCaptionTest {

    /** Stub manager that marks every key it resolves, so routing through i18n is observable. */
    private class MarkingManager : I18nManager {
        override fun gettext(key: String, vararg args: Any?): String = "tx:$key"
        override fun ngettext(singularKey: String, pluralKey: String, value: Int, vararg args: Any?): String =
            "tx:$singularKey"
    }

    private lateinit var previousManager: I18nManager

    @BeforeTest
    fun rememberManager() {
        previousManager = I18n.manager
    }

    @AfterTest
    fun restoreManager() {
        I18n.manager = previousManager
    }

    @Test
    fun captionPrefixRoutesThroughI18n() {
        I18n.manager = MarkingManager()
        assertEquals("tx:Help — Tasks", helpCaption("Tasks"), "the Help prefix must resolve through i18n")
    }

    @Test
    fun captionReflectsTheProviderValueAtEvaluationTime() {
        // Models the T-4 scenario: the label is empty when the FAB is built and becomes the real
        // item label later — the caption computed at open time must carry the current value.
        var label = ""
        val provider = { label }
        label = "Component: X-42"
        assertEquals("Help — Component: X-42", helpCaption(provider()),
            "the caption must be built from the provider's value at open time, not a captured copy")
    }
}
