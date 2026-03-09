package com.fonrouge.base.types

import com.fonrouge.base.serializers.EMPTY_OID
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Tests for ID type wrappers: OId, IntId, LongId, StringId.
 */
class IdTypesTest {

    // -- OId --

    @Test
    fun oidDefaultIdHas24Chars() {
        val oid = OId<Any>()
        assertEquals(24, oid.id.length, "OId should produce a 24-char hex string")
    }

    @Test
    fun oidWithExplicitValue() {
        val oid = OId<Any>("aabbccdd11223344aabbccdd")
        assertEquals("aabbccdd11223344aabbccdd", oid.id)
    }

    @Test
    fun oidEquality() {
        val a = OId<Any>("aabbccdd11223344aabbccdd")
        val b = OId<Any>("aabbccdd11223344aabbccdd")
        assertEquals(a, b)
    }

    @Test
    fun emptyOidHasAllZeros() {
        val empty = emptyOId<Any>()
        assertEquals(EMPTY_OID, empty.id)
        assertEquals(24, empty.id.length)
        assertEquals("000000000000000000000000", empty.id)
    }

    @Test
    fun toOidExtension() {
        val oid: OId<Any> = "abc123def456abc123def456".toOId()
        assertEquals("abc123def456abc123def456", oid.id)
    }

    // -- IntId --

    @Test
    fun intIdWrapsValue() {
        val id = IntId<Any>(42)
        assertEquals(42, id.id)
    }

    @Test
    fun intIdToString() {
        assertEquals("99", IntId<Any>(99).toString())
    }

    @Test
    fun intIdEquality() {
        assertEquals(IntId<Any>(1), IntId<Any>(1))
        assertNotEquals(IntId<Any>(1), IntId<Any>(2))
    }

    @Test
    fun toIntIdExtension() {
        val id: IntId<Any> = 7.toIntId()
        assertEquals(7, id.id)
    }

    // -- LongId --

    @Test
    fun longIdWrapsValue() {
        val id = LongId<Any>(123456789L)
        assertEquals(123456789L, id.id)
    }

    @Test
    fun longIdToString() {
        assertEquals("500", LongId<Any>(500L).toString())
    }

    @Test
    fun longIdEquality() {
        assertEquals(LongId<Any>(10L), LongId<Any>(10L))
        assertNotEquals(LongId<Any>(10L), LongId<Any>(20L))
    }

    @Test
    fun toLongIdExtension() {
        val id: LongId<Any> = 99L.toLongId()
        assertEquals(99L, id.id)
    }

    // -- StringId --

    @Test
    fun stringIdWrapsValue() {
        val id = StringId<Any>("hello")
        assertEquals("hello", id.id)
    }

    @Test
    fun stringIdToString() {
        assertEquals("test", StringId<Any>("test").toString())
    }

    @Test
    fun stringIdEquality() {
        assertEquals(StringId<Any>("a"), StringId<Any>("a"))
        assertNotEquals(StringId<Any>("a"), StringId<Any>("b"))
    }

    @Test
    fun toStringIdExtension() {
        val id: StringId<Any> = "myId".toStringId()
        assertEquals("myId", id.id)
    }

    // -- JSON serialization round-trips --

    @Serializable
    data class DocWithIntId(override val _id: IntId<DocWithIntId>, val name: String) :
        com.fonrouge.base.model.BaseDoc<IntId<DocWithIntId>>

    @Serializable
    data class DocWithLongId(override val _id: LongId<DocWithLongId>, val name: String) :
        com.fonrouge.base.model.BaseDoc<LongId<DocWithLongId>>

    @Serializable
    data class DocWithStringId(override val _id: StringId<DocWithStringId>, val name: String) :
        com.fonrouge.base.model.BaseDoc<StringId<DocWithStringId>>

    @Test
    fun intIdJsonRoundTrip() {
        val doc = DocWithIntId(IntId(42), "test")
        val json = Json.encodeToString(doc)
        val decoded = Json.decodeFromString<DocWithIntId>(json)
        assertEquals(doc, decoded)
    }

    @Test
    fun longIdJsonRoundTrip() {
        val doc = DocWithLongId(LongId(999L), "test")
        val json = Json.encodeToString(doc)
        val decoded = Json.decodeFromString<DocWithLongId>(json)
        assertEquals(doc, decoded)
    }

    @Test
    fun stringIdJsonRoundTrip() {
        val doc = DocWithStringId(StringId("abc"), "test")
        val json = Json.encodeToString(doc)
        val decoded = Json.decodeFromString<DocWithStringId>(json)
        assertEquals(doc, decoded)
    }
}
