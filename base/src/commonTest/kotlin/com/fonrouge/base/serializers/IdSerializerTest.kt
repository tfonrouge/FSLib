package com.fonrouge.base.serializers

import com.fonrouge.base.types.IntId
import com.fonrouge.base.types.LongId
import com.fonrouge.base.types.StringId
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for ID serializers: IntIdSerializer, LongIdSerializer, StringIdSerializer.
 */
class IdSerializerTest {

    @Serializable
    data class IntIdHolder(val id: IntId<IntIdHolder>)

    @Serializable
    data class LongIdHolder(val id: LongId<LongIdHolder>)

    @Serializable
    data class StringIdHolder(val id: StringId<StringIdHolder>)

    // -- IntIdSerializer --

    @Test
    fun intIdSerializesToInt() {
        val holder = IntIdHolder(IntId(42))
        val json = Json.encodeToString(holder)
        assertEquals("{\"id\":42}", json)
    }

    @Test
    fun intIdDeserializesFromInt() {
        val decoded = Json.decodeFromString<IntIdHolder>("{\"id\":42}")
        assertEquals(42, decoded.id.id)
    }

    @Test
    fun intIdRoundTrip() {
        val original = IntIdHolder(IntId(0))
        assertEquals(original, Json.decodeFromString(Json.encodeToString(original)))

        val negative = IntIdHolder(IntId(-1))
        assertEquals(negative, Json.decodeFromString(Json.encodeToString(negative)))

        val maxVal = IntIdHolder(IntId(Int.MAX_VALUE))
        assertEquals(maxVal, Json.decodeFromString(Json.encodeToString(maxVal)))
    }

    // -- LongIdSerializer --

    @Test
    fun longIdSerializesToLong() {
        val holder = LongIdHolder(LongId(9999999999L))
        val json = Json.encodeToString(holder)
        assertEquals("{\"id\":9999999999}", json)
    }

    @Test
    fun longIdDeserializesFromLong() {
        val decoded = Json.decodeFromString<LongIdHolder>("{\"id\":9999999999}")
        assertEquals(9999999999L, decoded.id.id)
    }

    @Test
    fun longIdRoundTrip() {
        val original = LongIdHolder(LongId(Long.MAX_VALUE))
        assertEquals(original, Json.decodeFromString(Json.encodeToString(original)))
    }

    // -- StringIdSerializer --

    @Test
    fun stringIdSerializesToString() {
        val holder = StringIdHolder(StringId("abc"))
        val json = Json.encodeToString(holder)
        assertEquals("{\"id\":\"abc\"}", json)
    }

    @Test
    fun stringIdDeserializesFromString() {
        val decoded = Json.decodeFromString<StringIdHolder>("{\"id\":\"hello\"}")
        assertEquals("hello", decoded.id.id)
    }

    @Test
    fun stringIdRoundTripWithSpecialChars() {
        val holder = StringIdHolder(StringId("hello \"world\" \\ /"))
        val decoded = Json.decodeFromString<StringIdHolder>(Json.encodeToString(holder))
        assertEquals(holder, decoded)
    }

    @Test
    fun stringIdEmptyString() {
        val holder = StringIdHolder(StringId(""))
        val decoded = Json.decodeFromString<StringIdHolder>(Json.encodeToString(holder))
        assertEquals("", decoded.id.id)
    }
}
