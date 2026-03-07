package com.fonrouge.base.state

import kotlinx.serialization.json.Json
import kotlin.test.*

/**
 * Tests for the State enum and SimpleState data class.
 */
class StateTest {

    // -- State enum --

    @Test
    fun stateEnumValues() {
        assertEquals(3, State.entries.size)
        assertEquals(State.Ok, State.valueOf("Ok"))
        assertEquals(State.Warn, State.valueOf("Warn"))
        assertEquals(State.Error, State.valueOf("Error"))
    }

    @Test
    fun stateJsonRoundTrip() {
        for (state in State.entries) {
            val json = Json.encodeToString(state)
            val decoded = Json.decodeFromString<State>(json)
            assertEquals(state, decoded)
        }
    }

    // -- SimpleState --

    @Test
    fun simpleStateOk() {
        val state = SimpleState(isOk = true, msgOk = "Done", msgError = null)
        assertEquals(State.Ok, state.state)
        assertEquals("Done", state.msgOk)
        assertNull(state.msgError)
        assertFalse(state.hasError)
    }

    @Test
    fun simpleStateError() {
        val state = SimpleState(isOk = false, msgOk = null, msgError = "Failed")
        assertEquals(State.Error, state.state)
        assertNull(state.msgOk)
        assertEquals("Failed", state.msgError)
        assertTrue(state.hasError)
    }

    @Test
    fun simpleStateWarn() {
        val state = simpleWarnState("Warning message")
        assertEquals(State.Warn, state.state)
        assertNull(state.msgOk)
        assertEquals("Warning message", state.msgError)
        assertFalse(state.hasError)
    }

    @Test
    fun simpleErrorStateHelper() {
        val state = simpleErrorState("Something failed")
        assertEquals(State.Error, state.state)
        assertEquals("Something failed", state.msgError)
        assertTrue(state.hasError)
    }

    @Test
    fun simpleStateDefaultMessages() {
        val ok = SimpleState(isOk = true)
        assertEquals(MSG_OK, ok.msgOk)

        val err = SimpleState(isOk = false)
        assertEquals(MSG_ERROR, err.msgError)
    }

    @Test
    fun simpleStateHasErrorOnlyForErrorState() {
        assertFalse(SimpleState(state = State.Ok).hasError)
        assertFalse(SimpleState(state = State.Warn).hasError)
        assertTrue(SimpleState(state = State.Error).hasError)
    }
}
