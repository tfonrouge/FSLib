package com.fonrouge.fullStack.view

import io.kvision.i18n.I18n
import io.kvision.i18n.I18nManager
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Pins that every string of the unsaved-changes cancel dialog resolves through KVision i18n
 * (blueprints/view-consumer-gaps CONTRACT I-4, OC-02): with a stubbed [I18nManager] installed,
 * all four texts come back translated — including Yes/No, whose KVision `Confirm` defaults
 * would otherwise be raw English literals invisible to a consumer catalog.
 */
class ConfirmCancelTextsTest {

    /** Stub manager that marks every key it resolves, so routing through i18n is observable. */
    private class MarkingManager : I18nManager {
        override fun gettext(key: String, vararg args: Any?): String = "tx:$key"
        override fun ngettext(singularKey: String, pluralKey: String, value: Int, vararg args: Any?): String =
            "tx:$singularKey"
    }

    private lateinit var previousManager: I18nManager

    @BeforeTest
    fun installMarkingManager() {
        previousManager = I18n.manager
        I18n.manager = MarkingManager()
    }

    @AfterTest
    fun restoreManager() {
        I18n.manager = previousManager
    }

    @Test
    fun allFourDialogStringsRouteThroughI18n() {
        val texts = confirmCancelTexts()
        assertEquals("tx:Please Confirm", texts.caption, "caption must resolve through i18n")
        assertEquals("tx:Cancel and forget current changes?", texts.text, "text must resolve through i18n")
        assertEquals("tx:Yes", texts.yes, "the Yes button must resolve through i18n")
        assertEquals("tx:No", texts.no, "the No button must resolve through i18n")
    }

    @Test
    fun defaultManagerPassesEnglishSourceKeysThrough() {
        I18n.manager = previousManager
        val texts = confirmCancelTexts()
        assertEquals("Please Confirm", texts.caption, "without a catalog the English source key is the text")
        assertEquals("Cancel and forget current changes?", texts.text)
    }
}
