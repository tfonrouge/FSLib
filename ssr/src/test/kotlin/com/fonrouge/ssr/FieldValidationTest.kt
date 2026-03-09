package com.fonrouge.ssr

import com.fonrouge.ssr.bind.FormBinder
import com.fonrouge.ssr.model.FieldDef
import com.fonrouge.ssr.model.FieldValidation
import io.ktor.http.*
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for field-level validation rules.
 */
class FieldValidationTest {

    @Test
    fun emailValidationAcceptsValid() {
        val fields = listOf(
            FieldDef<TestProduct>("name", "Name", "name").apply { email() },
        )
        val binder = FormBinder(TestProduct::class, fields)

        val result = binder.bindAndValidate(
            parametersOf(
                "_id" to listOf("1"),
                "name" to listOf("user@example.com"),
            )
        )

        assertFalse(result.errors.containsKey("name"), "Valid email should not produce errors")
    }

    @Test
    fun emailValidationRejectsInvalid() {
        val fields = listOf(
            FieldDef<TestProduct>("name", "Name", "name").apply { email() },
        )
        val binder = FormBinder(TestProduct::class, fields)

        val result = binder.bindAndValidate(
            parametersOf(
                "_id" to listOf("1"),
                "name" to listOf("not-an-email"),
            )
        )

        assertTrue(result.errors.containsKey("name"), "Invalid email should produce errors")
    }

    @Test
    fun minLengthValidation() {
        val fields = listOf(
            FieldDef<TestProduct>("name", "Name", "name").apply { minLength(5) },
        )
        val binder = FormBinder(TestProduct::class, fields)

        val tooShort = binder.bindAndValidate(
            parametersOf("_id" to listOf("1"), "name" to listOf("ab"))
        )
        assertTrue(tooShort.errors.containsKey("name"))

        val longEnough = binder.bindAndValidate(
            parametersOf("_id" to listOf("1"), "name" to listOf("abcde"))
        )
        assertFalse(longEnough.errors.containsKey("name"))
    }

    @Test
    fun patternValidation() {
        val fields = listOf(
            FieldDef<TestProduct>("name", "Name", "name").apply {
                pattern("^[A-Z].*", "Must start with uppercase")
            },
        )
        val binder = FormBinder(TestProduct::class, fields)

        val invalid = binder.bindAndValidate(
            parametersOf("_id" to listOf("1"), "name" to listOf("lowercase"))
        )
        assertTrue(invalid.errors.containsKey("name"))
        assertTrue(invalid.errors["name"]!!.any { it.contains("uppercase") })

        val valid = binder.bindAndValidate(
            parametersOf("_id" to listOf("1"), "name" to listOf("Uppercase"))
        )
        assertFalse(valid.errors.containsKey("name"))
    }

    @Test
    fun customValidation() {
        val fields = listOf(
            FieldDef<TestProduct>("name", "Name", "name").apply {
                validators.add(FieldValidation.Custom("no-spaces") { value ->
                    if (value != null && value.contains(" ")) "Name cannot contain spaces" else null
                })
            },
        )
        val binder = FormBinder(TestProduct::class, fields)

        val invalid = binder.bindAndValidate(
            parametersOf("_id" to listOf("1"), "name" to listOf("has space"))
        )
        assertTrue(invalid.errors.containsKey("name"))

        val valid = binder.bindAndValidate(
            parametersOf("_id" to listOf("1"), "name" to listOf("nospace"))
        )
        assertFalse(valid.errors.containsKey("name"))
    }

    @Test
    fun multipleValidationsOnSameField() {
        val fields = listOf(
            FieldDef<TestProduct>("name", "Name", "name").apply {
                required()
                minLength(3)
                maxLength(10)
            },
        )
        val binder = FormBinder(TestProduct::class, fields)

        // Empty — fails required
        val empty = binder.bindAndValidate(
            parametersOf("_id" to listOf("1"), "name" to listOf(""))
        )
        assertTrue(empty.errors.containsKey("name"))

        // Too short — fails minLength
        val short = binder.bindAndValidate(
            parametersOf("_id" to listOf("1"), "name" to listOf("ab"))
        )
        assertTrue(short.errors.containsKey("name"))

        // Too long — fails maxLength
        val long = binder.bindAndValidate(
            parametersOf("_id" to listOf("1"), "name" to listOf("a".repeat(11)))
        )
        assertTrue(long.errors.containsKey("name"))

        // Just right
        val ok = binder.bindAndValidate(
            parametersOf("_id" to listOf("1"), "name" to listOf("valid"))
        )
        assertFalse(ok.errors.containsKey("name"))
    }
}
