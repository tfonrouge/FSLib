package com.fonrouge.base

import com.fonrouge.base.types.OId
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests para la función `OId(OffsetDateTime)` que genera un OId basado en una fecha.
 */
class OIdFromDateTimeTest {

    @Test
    fun oidFromDateTimeProduces24CharHexString() {
        val dt = OffsetDateTime.of(2024, 1, 15, 12, 0, 0, 0, ZoneOffset.UTC)
        val oid = OId<Any>(dt)
        assertEquals(24, oid.id.length, "OId should be 24 characters")
    }

    @Test
    fun oidFromDateTimeContainsOnlyHexChars() {
        val dt = OffsetDateTime.of(2024, 1, 15, 12, 0, 0, 0, ZoneOffset.UTC)
        val oid = OId<Any>(dt)
        assertTrue(oid.id.all { it in "0123456789abcdef" }, "OId should contain only hex characters, got: '${oid.id}'")
    }

    @Test
    fun oidFromDateTimePadsWithZerosNotSpaces() {
        // Use epoch (0 seconds) to force maximum padding
        val dt = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        val oid = OId<Any>(dt)
        assertEquals("000000000000000000000000", oid.id, "OId from epoch should be all zeros")
        assertTrue(' ' !in oid.id, "OId should not contain spaces")
    }

    @Test
    fun oidFromDateTimeEncodesTimestampCorrectly() {
        // A known timestamp: 2024-01-15T12:00:00Z = 1705320000 seconds = 0x65A4E700
        val dt = OffsetDateTime.of(2024, 1, 15, 12, 0, 0, 0, ZoneOffset.UTC)
        val oid = OId<Any>(dt)
        val timestampHex = oid.id.substring(0, 8)
        val expectedHex = dt.toEpochSecond().toString(16).padStart(8, '0')
        assertEquals(expectedHex, timestampHex, "First 8 chars should be the hex timestamp")
    }
}
