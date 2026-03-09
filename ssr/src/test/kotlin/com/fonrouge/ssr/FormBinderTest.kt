package com.fonrouge.ssr

import com.fonrouge.ssr.bind.FormBinder
import com.fonrouge.ssr.model.FieldDef
import io.ktor.http.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for [FormBinder] — the reflection-based form parameter binder.
 */
class FormBinderTest {

    private val fields = listOf(
        FieldDef<TestProduct>("_id", "ID", "_id").apply { hidden() },
        FieldDef<TestProduct>("name", "Name", "name").apply { required(); maxLength(100) },
        FieldDef<TestProduct>("price", "Price", "price").apply { required(); number() },
        FieldDef<TestProduct>("category", "Category", "category").apply {
            select("Electronics", "Books", "Clothing")
        },
        FieldDef<TestProduct>("active", "Active", "active").apply { checkbox() },
    )

    private val binder = FormBinder(TestProduct::class, fields)

    @Test
    fun bindValidParameters() {
        val params = parametersOf(
            "_id" to listOf("prod-1"),
            "name" to listOf("Widget"),
            "price" to listOf("29.99"),
            "category" to listOf("Electronics"),
            "active" to listOf("on"),
        )

        val result = binder.bindAndValidate(params)

        assertFalse(result.hasErrors, "Expected no errors but got: ${result.errors}")
        assertNotNull(result.value)
        assertEquals("prod-1", result.value!!._id)
        assertEquals("Widget", result.value!!.name)
        assertEquals(29.99, result.value!!.price)
        assertEquals("Electronics", result.value!!.category)
        assertTrue(result.value!!.active)
    }

    @Test
    fun bindMissingRequiredField() {
        val params = parametersOf(
            "_id" to listOf("prod-1"),
            "name" to listOf(""),  // blank — required
            "price" to listOf("10.0"),
            "category" to listOf("Books"),
        )

        val result = binder.bindAndValidate(params)

        assertTrue(result.hasErrors)
        assertTrue(result.errors.containsKey("name"), "Expected error on 'name' field")
    }

    @Test
    fun bindInvalidNumber() {
        val params = parametersOf(
            "_id" to listOf("prod-1"),
            "name" to listOf("Widget"),
            "price" to listOf("not-a-number"),
            "category" to listOf("Books"),
        )

        val result = binder.bindAndValidate(params)

        assertTrue(result.hasErrors)
        assertTrue(result.errors.containsKey("price"), "Expected error on 'price' field")
    }

    @Test
    fun bindMaxLengthViolation() {
        val params = parametersOf(
            "_id" to listOf("prod-1"),
            "name" to listOf("A".repeat(101)),  // exceeds maxLength(100)
            "price" to listOf("10.0"),
            "category" to listOf("Books"),
        )

        val result = binder.bindAndValidate(params)

        assertTrue(result.hasErrors)
        assertTrue(result.errors.containsKey("name"), "Expected error on 'name' field")
    }

    @Test
    fun bindCheckboxUnchecked() {
        // HTML checkboxes send nothing when unchecked
        val params = parametersOf(
            "_id" to listOf("prod-1"),
            "name" to listOf("Widget"),
            "price" to listOf("10.0"),
            "category" to listOf("Books"),
            // active is missing — should default to false or use constructor default
        )

        val result = binder.bindAndValidate(params)

        assertFalse(result.hasErrors, "Expected no errors but got: ${result.errors}")
        assertNotNull(result.value)
    }

    @Test
    fun bindPreservesRawValues() {
        val params = parametersOf(
            "_id" to listOf("prod-1"),
            "name" to listOf(""),  // will fail required
            "price" to listOf("10.0"),
            "category" to listOf("Books"),
        )

        val result = binder.bindAndValidate(params)

        assertTrue(result.hasErrors)
        assertEquals("", result.rawValues["name"])
        assertEquals("10.0", result.rawValues["price"])
        assertEquals("Books", result.rawValues["category"])
    }

    @Test
    fun bindWithExistingItem() {
        val existing = TestProduct(
            _id = "prod-1",
            name = "Old Name",
            price = 5.0,
            category = "Books",
            active = false,
        )

        val params = parametersOf(
            "_id" to listOf("prod-1"),
            "name" to listOf("New Name"),
            "price" to listOf("15.0"),
            "category" to listOf("Electronics"),
            "active" to listOf("on"),
        )

        val result = binder.bindAndValidate(params, existing)

        assertFalse(result.hasErrors, "Expected no errors but got: ${result.errors}")
        assertNotNull(result.value)
        assertEquals("prod-1", result.value!!._id)
        assertEquals("New Name", result.value!!.name)
        assertEquals(15.0, result.value!!.price)
        assertEquals("Electronics", result.value!!.category)
        assertTrue(result.value!!.active)
    }

    @Test
    fun bindEmptyFormReturnsErrors() {
        val params = parametersOf()

        val result = binder.bindAndValidate(params)

        assertTrue(result.hasErrors)
        assertNull(result.value)
    }
}
