package com.fonrouge.ssr

import com.fonrouge.ssr.model.FieldType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [PageDef] — column/field definition DSL.
 */
class PageDefTest {

    private val repo = MockRepository()
    private val pageDef = TestProductPageDef(repo)

    @Test
    fun pageDefHasCorrectMetadata() {
        assertEquals("Products", pageDef.title)
        assertEquals("Product", pageDef.titleItem)
        assertEquals("/products", pageDef.basePath)
        assertEquals(25, pageDef.pageSize)
    }

    @Test
    fun columnsAreRegistered() {
        assertEquals(4, pageDef.columns.size)
        assertEquals("name", pageDef.columns[0].name)
        assertEquals("Name", pageDef.columns[0].label)
        assertTrue(pageDef.columns[0].sortable)
        assertTrue(pageDef.columns[0].filterable)

        assertEquals("price", pageDef.columns[1].name)
        assertTrue(pageDef.columns[1].sortable)
        assertFalse(pageDef.columns[1].filterable)

        assertEquals("category", pageDef.columns[2].name)
        assertFalse(pageDef.columns[2].sortable)
    }

    @Test
    fun fieldsAreRegistered() {
        assertEquals(5, pageDef.fields.size)

        // _id field
        assertEquals("_id", pageDef.fields[0].name)
        assertEquals(FieldType.Hidden, pageDef.fields[0].type)

        // name field
        assertEquals("name", pageDef.fields[1].name)
        assertEquals("Name", pageDef.fields[1].label)
        assertTrue(pageDef.fields[1].required)
        assertEquals(100, pageDef.fields[1].maxLength)
        assertEquals(6, pageDef.fields[1].colWidth)

        // price field
        assertEquals("price", pageDef.fields[2].name)
        assertEquals(FieldType.Number, pageDef.fields[2].type)
        assertEquals(3, pageDef.fields[2].colWidth)

        // category field
        assertEquals("category", pageDef.fields[3].name)
        assertEquals(FieldType.Select, pageDef.fields[3].type)
        assertEquals(3, pageDef.fields[3].options.size)
        assertEquals("Electronics" to "Electronics", pageDef.fields[3].options[0])

        // active field
        assertEquals("active", pageDef.fields[4].name)
        assertEquals(FieldType.Checkbox, pageDef.fields[4].type)
    }

    @Test
    fun columnAccessorExtractsValue() {
        val product = TestProduct(_id = "1", name = "Widget", price = 9.99, category = "Books", active = true)

        assertEquals("Widget", pageDef.columns[0].accessor(product))
        assertEquals("9.99", pageDef.columns[1].accessor(product))
        assertEquals("Books", pageDef.columns[2].accessor(product))
    }

    @Test
    fun columnBadgeRendersHtml() {
        val product = TestProduct(_id = "1", name = "X", active = true)
        val activeCol = pageDef.columns[3]

        val html = activeCol.renderHtml?.invoke(product)
        assertFalse(html.isNullOrBlank())
        assertTrue(html!!.contains("badge"))
        assertTrue(html.contains("bg-success"))
    }

    @Test
    fun parseIdReturnsString() {
        assertEquals("abc-123", pageDef.parseId("abc-123"))
    }
}
