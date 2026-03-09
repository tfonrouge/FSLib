package com.fonrouge.ssr

import com.fonrouge.ssr.bind.TypeConverters
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [TypeConverters] — string-to-type conversion functions.
 */
class TypeConvertersTest {

    @Test
    fun toIntValid() {
        assertEquals(42, TypeConverters.toInt("42").getOrNull())
    }

    @Test
    fun toIntInvalid() {
        assertTrue(TypeConverters.toInt("abc").isFailure)
    }

    @Test
    fun toIntNegative() {
        assertEquals(-7, TypeConverters.toInt("-7").getOrNull())
    }

    @Test
    fun toLongValid() {
        assertEquals(9999999999L, TypeConverters.toLong("9999999999").getOrNull())
    }

    @Test
    fun toLongInvalid() {
        assertTrue(TypeConverters.toLong("xyz").isFailure)
    }

    @Test
    fun toDoubleValid() {
        assertEquals(3.14, TypeConverters.toDouble("3.14").getOrNull())
    }

    @Test
    fun toDoubleInvalid() {
        assertTrue(TypeConverters.toDouble("not-num").isFailure)
    }

    @Test
    fun toBooleanTrue() {
        assertTrue(TypeConverters.toBoolean("true").getOrNull() as Boolean)
        assertTrue(TypeConverters.toBoolean("on").getOrNull() as Boolean)
        assertTrue(TypeConverters.toBoolean("1").getOrNull() as Boolean)
        assertTrue(TypeConverters.toBoolean("TRUE").getOrNull() as Boolean)
    }

    @Test
    fun toBooleanFalse() {
        assertFalse(TypeConverters.toBoolean("false").getOrNull() as Boolean)
        assertFalse(TypeConverters.toBoolean("").getOrNull() as Boolean)
        assertFalse(TypeConverters.toBoolean("0").getOrNull() as Boolean)
        assertFalse(TypeConverters.toBoolean("no").getOrNull() as Boolean)
    }

    @Test
    fun toLocalDateValid() {
        val result = TypeConverters.toLocalDate("2024-03-15").getOrNull()
        assertTrue(result is LocalDate)
        assertEquals(2024, (result as LocalDate).year)
        assertEquals(3, result.monthNumber)
        assertEquals(15, result.dayOfMonth)
    }

    @Test
    fun toLocalDateInvalid() {
        assertTrue(TypeConverters.toLocalDate("not-a-date").isFailure)
    }

    @Test
    fun toLocalDateTimeValid() {
        val result = TypeConverters.toLocalDateTime("2024-03-15T10:30:00")
        assertTrue(result.isSuccess)
    }

    @Test
    fun toLocalDateTimeWithSpace() {
        // HTML datetime-local uses space in some browsers
        val result = TypeConverters.toLocalDateTime("2024-03-15 10:30:00")
        assertTrue(result.isSuccess)
    }

    @Test
    fun toEnumValid() {
        val result = TypeConverters.toEnum("Electronics", TestCategory::class.java)
        assertTrue(result.isSuccess)
        assertEquals(TestCategory.Electronics, result.getOrNull())
    }

    @Test
    fun toEnumCaseInsensitive() {
        val result = TypeConverters.toEnum("electronics", TestCategory::class.java)
        assertTrue(result.isSuccess)
        assertEquals(TestCategory.Electronics, result.getOrNull())
    }

    @Test
    fun toEnumInvalid() {
        val result = TypeConverters.toEnum("NonExistent", TestCategory::class.java)
        assertTrue(result.isFailure)
    }
}

/** Test enum for TypeConverters tests. */
enum class TestCategory {
    Electronics, Books, Clothing
}
