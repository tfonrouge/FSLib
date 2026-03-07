package com.fonrouge.base.api

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for CrudTask enum and its extension properties.
 */
class CrudTaskTest {

    @Test
    fun crudTaskEncodedValues() {
        assertEquals("C", CrudTask.Create.encoded)
        assertEquals("R", CrudTask.Read.encoded)
        assertEquals("U", CrudTask.Update.encoded)
        assertEquals("D", CrudTask.Delete.encoded)
    }

    @Test
    fun crudTaskEntryCount() {
        assertEquals(4, CrudTask.entries.size)
    }

    @Test
    fun isUpsertDeleteForCreate() {
        assertTrue(CrudTask.Create.isUpsertDelete)
    }

    @Test
    fun isUpsertDeleteForRead() {
        assertFalse(CrudTask.Read.isUpsertDelete)
    }

    @Test
    fun isUpsertDeleteForUpdate() {
        assertTrue(CrudTask.Update.isUpsertDelete)
    }

    @Test
    fun isUpsertDeleteForDelete() {
        assertTrue(CrudTask.Delete.isUpsertDelete)
    }

    @Test
    fun crudTaskJsonRoundTrip() {
        for (task in CrudTask.entries) {
            val json = Json.encodeToString(task)
            val decoded = Json.decodeFromString<CrudTask>(json)
            assertEquals(task, decoded)
        }
    }

    @Test
    fun crudTaskJsonSerialNames() {
        assertEquals("\"C\"", Json.encodeToString(CrudTask.Create))
        assertEquals("\"R\"", Json.encodeToString(CrudTask.Read))
        assertEquals("\"U\"", Json.encodeToString(CrudTask.Update))
        assertEquals("\"D\"", Json.encodeToString(CrudTask.Delete))
    }
}
