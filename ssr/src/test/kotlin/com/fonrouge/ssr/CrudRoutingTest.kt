package com.fonrouge.ssr

import com.fonrouge.ssr.plugin.FsSsr
import com.fonrouge.ssr.session.FlashSession
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Integration tests for SSR CRUD routing.
 * Uses Ktor test host to verify route generation and behavior.
 */
class CrudRoutingTest {

    private fun testSsrApp(
        seedData: (MockRepository) -> Unit = {},
        block: suspend ApplicationTestBuilder.(MockRepository) -> Unit,
    ) = testApplication {
        val repo = MockRepository()
        seedData(repo)
        val pageDef = TestProductPageDef(repo)

        application {
            install(Sessions) {
                cookie<FlashSession>("FSS_FLASH")
            }
            install(FsSsr) {
                page(pageDef)
            }
        }

        block(repo)
    }

    // ── LIST ──

    @Test
    fun listPageReturnsHtml() = testSsrApp(
        seedData = { repo ->
            repo.store["1"] = TestProduct(_id = "1", name = "Widget", price = 9.99, category = "Electronics")
            repo.store["2"] = TestProduct(_id = "2", name = "Book", price = 14.99, category = "Books")
        }
    ) { _ ->
        val response = client.get("/products")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("Products"), "Should contain page title")
        assertTrue(body.contains("Widget"), "Should contain product name")
        assertTrue(body.contains("Book"), "Should contain second product name")
        assertTrue(body.contains("<table"), "Should contain a table element")
    }

    @Test
    fun emptyListShowsNoRecords() = testSsrApp { _ ->
        val response = client.get("/products")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("No records found"))
    }

    // ── CREATE ──

    @Test
    fun newFormReturnsHtml() = testSsrApp { _ ->
        val response = client.get("/products/new")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("New Product"), "Should contain form title")
        assertTrue(body.contains("<form"), "Should contain a form element")
        assertTrue(body.contains("Name"), "Should contain field label")
    }

    @Test
    fun createPostInsertsAndRedirects() = testSsrApp { repo ->
        val response = client.post("/products") {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody("_id=prod-1&name=Widget&price=29.99&category=Electronics&active=on")
        }

        // Should redirect after successful create
        assertEquals(HttpStatusCode.Found, response.status)
        // Item should be in the store
        assertEquals("Widget", repo.store["prod-1"]?.name)
        assertEquals(29.99, repo.store["prod-1"]?.price)
        assertEquals("insert", repo.lastOperation)
    }

    @Test
    fun createPostWithValidationErrorReRendersForm() = testSsrApp { _ ->
        val response = client.post("/products") {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody("_id=prod-1&name=&price=29.99&category=Electronics") // name is blank (required)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("is required"), "Should show validation error")
        assertTrue(body.contains("<form"), "Should re-render the form")
    }

    // ── READ ──

    @Test
    fun detailPageShowsItem() = testSsrApp(
        seedData = { repo ->
            repo.store["prod-1"] = TestProduct(_id = "prod-1", name = "Widget", price = 9.99, category = "Electronics")
        }
    ) { _ ->
        val response = client.get("/products/prod-1")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("Widget"), "Should contain item name")
        assertTrue(body.contains("Edit"), "Should have Edit button")
        assertFalse(body.contains("<form"), "Read view should not have a form tag")
    }

    @Test
    fun detailPageNotFoundRedirects() = testSsrApp { _ ->
        val response = client.get("/products/nonexistent")

        // The test client follows redirects, so we get the list page (200)
        // after the redirect from the not-found detail page
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("Products"), "Should show list page after redirect")
    }

    // ── UPDATE ──

    @Test
    fun editFormShowsItemData() = testSsrApp(
        seedData = { repo ->
            repo.store["prod-1"] = TestProduct(_id = "prod-1", name = "Widget", price = 9.99, category = "Electronics")
        }
    ) { _ ->
        val response = client.get("/products/prod-1/edit")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("Edit Product"), "Should contain edit title")
        assertTrue(body.contains("<form"), "Should contain a form")
        assertTrue(body.contains("Widget"), "Should contain current value")
    }

    @Test
    fun updatePostModifiesAndRedirects() = testSsrApp(
        seedData = { repo ->
            repo.store["prod-1"] = TestProduct(_id = "prod-1", name = "Widget", price = 9.99, category = "Electronics")
        }
    ) { repo ->
        val response = client.post("/products/prod-1") {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody("_id=prod-1&name=Updated Widget&price=19.99&category=Books&active=on")
        }

        assertEquals(HttpStatusCode.Found, response.status)
        assertEquals("Updated Widget", repo.store["prod-1"]?.name)
        assertEquals(19.99, repo.store["prod-1"]?.price)
        assertEquals("update", repo.lastOperation)
    }

    // ── DELETE ──

    @Test
    fun deletePostRemovesAndRedirects() = testSsrApp(
        seedData = { repo ->
            repo.store["prod-1"] = TestProduct(_id = "prod-1", name = "Widget", price = 9.99, category = "Electronics")
        }
    ) { repo ->
        val response = client.post("/products/prod-1/delete")

        assertEquals(HttpStatusCode.Found, response.status)
        assertFalse(repo.store.containsKey("prod-1"), "Item should be removed from store")
        assertEquals("delete", repo.lastOperation)
    }

    @Test
    fun deleteNonexistentRedirectsWithError() = testSsrApp { _ ->
        val response = client.post("/products/nonexistent/delete")

        assertEquals(HttpStatusCode.Found, response.status)
    }
}
