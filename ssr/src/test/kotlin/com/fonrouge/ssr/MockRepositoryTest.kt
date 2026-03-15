package com.fonrouge.ssr

import com.fonrouge.base.api.ApiFilter
import com.fonrouge.base.api.ApiList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for the [MockRepository] to verify it behaves correctly for CRUD routing tests.
 */
class MockRepositoryTest {

    private val repo = MockRepository()
    private val filter = ApiFilter()

    @Test
    fun insertAndFindById() = runTest {
        val product = TestProduct(_id = "p1", name = "Widget", price = 9.99)

        val insertResult = repo.insertOne(product, filter)
        assertFalse(insertResult.hasError)
        assertNotNull(insertResult.item)

        val found = repo.findById("p1", filter)
        assertNotNull(found)
        assertEquals("Widget", found.name)
    }

    @Test
    fun findByIdReturnsNullForMissing() = runTest {
        val found = repo.findById("nonexistent", filter)
        assertNull(found)
    }

    @Test
    fun updateModifiesItem() = runTest {
        repo.store["p1"] = TestProduct(_id = "p1", name = "Old", price = 5.0)

        val updated = TestProduct(_id = "p1", name = "New", price = 10.0)
        val result = repo.updateOne(updated, filter)

        assertFalse(result.hasError)
        assertEquals("New", repo.store["p1"]?.name)
        assertEquals(10.0, repo.store["p1"]?.price)
    }

    @Test
    fun deleteRemovesItem() = runTest {
        repo.store["p1"] = TestProduct(_id = "p1", name = "Widget")

        val result = repo.deleteOne("p1", filter)

        assertFalse(result.hasError)
        assertFalse(repo.store.containsKey("p1"))
    }

    @Test
    fun deleteNonexistentReturnsError() = runTest {
        val result = repo.deleteOne("nonexistent", filter)
        assertTrue(result.hasError)
    }

    @Test
    fun apiListProcessPaginates() = runTest {
        for (i in 1..30) {
            repo.store["p$i"] = TestProduct(_id = "p$i", name = "Product $i", price = i.toDouble())
        }

        val page1 = repo.apiListProcess(null, ApiList(tabPage = 1, tabSize = 10, apiFilter = filter))
        assertEquals(10, page1.data.size)
        assertEquals(3, page1.last_page)
        assertEquals(30, page1.last_row)

        val page3 = repo.apiListProcess(null, ApiList(tabPage = 3, tabSize = 10, apiFilter = filter))
        assertEquals(10, page3.data.size)
    }
}
