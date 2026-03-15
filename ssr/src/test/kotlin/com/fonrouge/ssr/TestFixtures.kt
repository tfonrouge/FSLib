package com.fonrouge.ssr

import com.fonrouge.base.api.*
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.model.BaseDoc
import com.fonrouge.base.state.ItemState
import com.fonrouge.base.state.ListState
import com.fonrouge.base.state.SimpleState
import com.fonrouge.base.state.State
import com.fonrouge.fullStack.repository.IChangeLogRepository
import com.fonrouge.fullStack.repository.IRepository
import com.fonrouge.fullStack.repository.IUserRepository
import io.ktor.server.application.*
import kotlinx.serialization.Serializable
import kotlin.reflect.KProperty1

// ── Test Model ──────────────────────────────────────────────

/**
 * Simple test model for SSR module tests.
 */
@Serializable
data class TestProduct(
    override val _id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val active: Boolean = true,
) : BaseDoc<String>

/**
 * Common container providing metadata for [TestProduct].
 */
object CommonTestProduct : ICommonContainer<TestProduct, String, ApiFilter>(
    itemKClass = TestProduct::class,
    filterKClass = ApiFilter::class,
    labelItem = "Product",
    labelList = "Products",
)

// ── Mock Repository ─────────────────────────────────────────

/**
 * In-memory mock implementation of [IRepository] for testing.
 * Stores items in a mutable map and supports basic CRUD operations.
 */
class MockRepository : IRepository<
    TestProduct,
    String,
    ApiFilter,
    String,
> {
    /** In-memory data store. */
    val store = mutableMapOf<String, TestProduct>()

    /** Tracks the last operation for assertions. */
    var lastOperation: String? = null

    override val commonContainer = CommonTestProduct
    override val readOnly = false
    override val readOnlyErrorMsg = "Read only"
    override val changeLogCollFun: () -> IChangeLogRepository? = { null }
    override val dependencies: (() -> List<IRepository.Dependency<*, String>>)? = null
    override val userCollFun: () -> IUserRepository<*, String>? = { null }

    override suspend fun insertOne(item: TestProduct, apiFilter: ApiFilter, call: ApplicationCall?): ItemState<TestProduct> {
        lastOperation = "insert"
        store[item._id] = item
        return ItemState(item = item)
    }

    override suspend fun findById(id: String?, apiFilter: ApiFilter): TestProduct? {
        lastOperation = "findById"
        return store[id]
    }

    override suspend fun findItemStateById(id: String?, apiFilter: ApiFilter): ItemState<TestProduct> {
        val item = store[id]
        return if (item != null) ItemState(item = item) else ItemState()
    }

    override suspend fun updateOne(item: TestProduct, apiFilter: ApiFilter, call: ApplicationCall?): ItemState<TestProduct> {
        lastOperation = "update"
        store[item._id] = item
        return ItemState(item = item)
    }

    override suspend fun deleteOne(id: String, apiFilter: ApiFilter): ItemState<TestProduct> {
        lastOperation = "delete"
        val item = store.remove(id)
        return if (item != null) ItemState(item = item) else ItemState(
            state = State.Error,
            msgError = "Not found",
        )
    }

    override suspend fun apiItemProcess(call: ApplicationCall?, iApiItem: IApiItem<TestProduct, String, ApiFilter>): ItemState<TestProduct> {
        return ItemState()
    }

    override suspend fun apiListProcess(call: ApplicationCall?, apiList: ApiList<ApiFilter>): ListState<TestProduct> {
        lastOperation = "list"
        val page = apiList.tabPage ?: 1
        val size = apiList.tabSize ?: 25
        val all = store.values.toList()
        val start = (page - 1) * size
        val pageData = all.drop(start).take(size)
        val lastPage = if (all.isEmpty()) 1 else ((all.size - 1) / size) + 1
        return ListState(
            data = pageData,
            last_page = lastPage,
            last_row = all.size,
        )
    }

    override suspend fun findList(apiFilter: ApiFilter): List<TestProduct> = store.values.toList()
    override suspend fun findOne(apiFilter: ApiFilter): TestProduct? = store.values.firstOrNull()

    override suspend fun getCrudPermission(call: ApplicationCall, crudTask: CrudTask): SimpleState {
        return SimpleState(State.Ok)
    }

    override suspend fun existsByField(property: KProperty1<out BaseDoc<*>, *>, value: Any?): Boolean = false
    override suspend fun findChildrenNot(item: TestProduct): ItemState<TestProduct> = ItemState(item = item)

    // Lifecycle hooks — all no-op for testing
    override suspend fun onQueryCreate(apiItem: ApiItem.Query.Create<TestProduct, String, ApiFilter>) = SimpleState(State.Ok)
    override suspend fun onQueryCreateItem(apiItem: ApiItem.Query.Create<TestProduct, String, ApiFilter>) = ItemState<TestProduct>()
    override suspend fun onQueryRead(apiItem: ApiItem.Query.Read<TestProduct, String, ApiFilter>) = SimpleState(State.Ok)
    override suspend fun onQueryUpdate(apiItem: ApiItem.Query.Update<TestProduct, String, ApiFilter>, orig: TestProduct) = SimpleState(State.Ok)
    override suspend fun onQueryDelete(apiItem: ApiItem.Query.Delete<TestProduct, String, ApiFilter>, item: TestProduct) = SimpleState(State.Ok)
    override suspend fun onQueryUpsert(apiItem: ApiItem.Query<TestProduct, String, ApiFilter>, orig: TestProduct?) = SimpleState(State.Ok)
    override suspend fun onBeforeCreateAction(apiItem: ApiItem.Action.Create<TestProduct, String, ApiFilter>) = ItemState<TestProduct>()
    override suspend fun onBeforeUpdateAction(apiItem: ApiItem.Action.Update<TestProduct, String, ApiFilter>, orig: TestProduct) = ItemState<TestProduct>()
    override suspend fun onBeforeDeleteAction(apiItem: ApiItem.Action.Delete<TestProduct, String, ApiFilter>) = ItemState<TestProduct>()
    override suspend fun onBeforeUpsertAction(apiItem: ApiItem.Action<TestProduct, String, ApiFilter>, orig: TestProduct?) = ItemState<TestProduct>()
    override suspend fun onAfterCreateAction(apiItem: ApiItem.Action.Create<TestProduct, String, ApiFilter>, result: Boolean) {}
    override suspend fun onAfterUpdateAction(apiItem: ApiItem.Action.Update<TestProduct, String, ApiFilter>, orig: TestProduct, result: Boolean) {}
    override suspend fun onAfterDeleteAction(apiItem: ApiItem.Action.Delete<TestProduct, String, ApiFilter>, result: Boolean) {}
    override suspend fun onAfterUpsertAction(apiItem: ApiItem.Action<TestProduct, String, ApiFilter>, orig: TestProduct?, result: Boolean) {}
    override suspend fun onAfterOpen() {}
    override suspend fun onValidate(apiItem: ApiItem.Action<TestProduct, String, ApiFilter>, item: TestProduct) = SimpleState(State.Ok)
    override suspend fun asApiItem(apiItem: ApiItem<TestProduct, String, ApiFilter>) = ItemState(item = apiItem)
}

// ── Test PageDef ────────────────────────────────────────────

/**
 * PageDef for [TestProduct] used in integration tests.
 */
class TestProductPageDef(repo: MockRepository) : PageDef<
    TestProduct,
    String,
    ApiFilter,
>(
    commonContainer = CommonTestProduct,
    repository = repo,
    title = "Products",
    titleItem = "Product",
    basePath = "/products",
) {
    init {
        column(TestProduct::name, "Name") { sortable(); filterable() }
        column(TestProduct::price, "Price") { sortable() }
        column(TestProduct::category, "Category")
        column(TestProduct::active, "Active") { badge(mapOf("true" to "success", "false" to "secondary")) }

        field(TestProduct::_id) { hidden() }
        field(TestProduct::name, "Name") { required(); maxLength(100); col(6) }
        field(TestProduct::price, "Price") { required(); number(); col(3) }
        field(TestProduct::category, "Category") { select("Electronics", "Books", "Clothing"); col(3) }
        field(TestProduct::active, "Active") { checkbox() }
    }

    override fun parseId(raw: String): String = raw
}
