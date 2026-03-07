package com.fonrouge.base.state

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for ListState and its utility functions.
 */
class ListStateTest {

    @Test
    fun listStateWithData() {
        val state = ListState(data = listOf("a", "b", "c"))
        assertEquals(3, state.data.size)
        assertEquals(State.Ok, state.state)
        assertFalse(state.hasError)
    }

    @Test
    fun listStateEmpty() {
        val state = ListState<String>()
        assertTrue(state.data.isEmpty())
        assertEquals(State.Ok, state.state)
    }

    @Test
    fun listStateWithPagination() {
        val state = ListState(
            data = listOf(1, 2, 3),
            last_page = 5,
            last_row = 50
        )
        assertEquals(5, state.last_page)
        assertEquals(50, state.last_row)
    }

    @Test
    fun listStateWithError() {
        val state = ListState<String>(state = State.Error, msgError = "DB timeout")
        assertTrue(state.hasError)
        assertEquals("DB timeout", state.msgError)
        assertTrue(state.data.isEmpty())
    }

    @Test
    fun setListCreatesNewInstance() {
        val original = ListState(data = listOf("a"))
        val updated = original.setList(listOf("x", "y"))
        assertEquals(listOf("x", "y"), updated.data)
        assertEquals(listOf("a"), original.data, "Original should be unchanged")
    }

    @Test
    fun listStateBuilderFunction() {
        val state = listState<String>(data = emptyList())
        assertTrue(state.data.isEmpty())
        assertEquals(State.Ok, state.state)
    }

    @Test
    fun listStateJsonRoundTrip() {
        val state = ListState(
            data = listOf("hello", "world"),
            last_page = 3,
            last_row = 30
        )
        val json = Json.encodeToString(state)
        val decoded = Json.decodeFromString<ListState<String>>(json)
        assertEquals(state.data, decoded.data)
        assertEquals(state.last_page, decoded.last_page)
        assertEquals(state.last_row, decoded.last_row)
    }

    @Test
    fun listStateJsonAlwaysEncodesData() {
        val state = ListState<String>()
        val json = Json.encodeToString(state)
        assertTrue(json.contains("\"data\""), "data field should always be present in JSON")
    }
}
