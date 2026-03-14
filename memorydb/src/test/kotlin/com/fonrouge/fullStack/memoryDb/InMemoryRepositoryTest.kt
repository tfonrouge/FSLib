package com.fonrouge.fullStack.memoryDb

import com.fonrouge.base.api.ApiList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for [InMemoryRepository] verifying CRUD operations, pagination, and lifecycle behavior.
 */
class InMemoryRepositoryTest {

    private fun createRepo() = InMemoryRepository<CommonTestItem, TestItem, String, TestFilter, String>(
        commonContainer = CommonTestItem,
    )

    private fun seededRepo() = createRepo().seed(
        listOf(
            TestItem(_id = "1", name = "Alpha", price = 10.0, category = "A"),
            TestItem(_id = "2", name = "Beta", price = 20.0, category = "B"),
            TestItem(_id = "3", name = "Gamma", price = 30.0, category = "A"),
        )
    )

    // ── CRUD ────────────────────────────────────────────────────

    @Test
    fun insertAndFindById() = runTest {
        val repo = createRepo()
        val item = TestItem(_id = "x1", name = "Widget", price = 9.99)
        val filter = TestFilter()

        val result = repo.insertOne(item, filter)
        assertFalse(result.hasError)
        assertNotNull(result.item)

        val found = repo.findById("x1", filter)
        assertNotNull(found)
        assertEquals("Widget", found.name)
    }

    @Test
    fun findByIdReturnsNullForMissing() = runTest {
        val repo = createRepo()
        val found = repo.findById("nonexistent", TestFilter())
        assertNull(found)
    }

    @Test
    fun updateModifiesItem() = runTest {
        val repo = seededRepo()
        val filter = TestFilter()

        val updated = TestItem(_id = "1", name = "Alpha Updated", price = 15.0)
        val result = repo.updateOne(updated, filter)

        assertFalse(result.hasError)
        val found = repo.findById("1", filter)
        assertEquals("Alpha Updated", found?.name)
        assertEquals(15.0, found?.price)
    }

    @Test
    fun deleteRemovesItem() = runTest {
        val repo = seededRepo()
        val filter = TestFilter()

        val result = repo.deleteOne("2", filter)
        assertFalse(result.hasError)

        val found = repo.findById("2", filter)
        assertNull(found)
    }

    @Test
    fun deleteNonexistentReturnsError() = runTest {
        val repo = createRepo()
        val result = repo.deleteOne("nonexistent", TestFilter())
        assertTrue(result.hasError)
    }

    // ── Seed ────────────────────────────────────────────────────

    @Test
    fun seedPopulatesStore() = runTest {
        val repo = seededRepo()
        val filter = TestFilter()

        val list = repo.findList(filter)
        assertEquals(3, list.size)
    }

    // ── Pagination ──────────────────────────────────────────────

    @Test
    fun apiListProcessPaginates() = runTest {
        val repo = createRepo()
        for (i in 1..25) {
            repo.insertOne(TestItem(_id = "p$i", name = "Product $i", price = i.toDouble()), TestFilter())
        }

        val page1 = repo.apiListProcess(null, ApiList(tabPage = 1, tabSize = 10, apiFilter = TestFilter()))
        assertEquals(10, page1.data.size)
        assertEquals(3, page1.last_page)
        assertEquals(25, page1.last_row)

        val page3 = repo.apiListProcess(null, ApiList(tabPage = 3, tabSize = 10, apiFilter = TestFilter()))
        assertEquals(5, page3.data.size)
    }

    @Test
    fun apiListProcessEmptyRepo() = runTest {
        val repo = createRepo()
        val result = repo.apiListProcess(null, ApiList(tabPage = 1, tabSize = 10, apiFilter = TestFilter()))
        assertEquals(0, result.data.size)
    }

    // ── FindList / FindOne ──────────────────────────────────────

    @Test
    fun findListReturnsAllItems() = runTest {
        val repo = seededRepo()
        val list = repo.findList(TestFilter())
        assertEquals(3, list.size)
    }

    @Test
    fun findOneReturnsFirstMatch() = runTest {
        val repo = seededRepo()
        val item = repo.findOne(TestFilter())
        assertNotNull(item)
    }

    @Test
    fun findOneReturnsNullOnEmpty() = runTest {
        val repo = createRepo()
        val item = repo.findOne(TestFilter())
        assertNull(item)
    }

    // ── ReadOnly ────────────────────────────────────────────────

    @Test
    fun readOnlyBlocksInsert() = runTest {
        val repo = InMemoryRepository<CommonTestItem, TestItem, String, TestFilter, String>(
            commonContainer = CommonTestItem,
            readOnly = true,
        )
        val result = repo.insertOne(TestItem(_id = "x", name = "Test"), TestFilter())
        assertTrue(result.hasError)
    }
}
