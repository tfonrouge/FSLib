package com.example.ssrsample.basic

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
import kotlin.reflect.KProperty1

/**
 * Minimal in-memory [IRepository] for the basic sample.
 */
class InMemoryRepository<T : BaseDoc<String>, FILT : IApiFilter<*>>(
    container: ICommonContainer<T, String, FILT>,
) : IRepository<T, String, FILT, String> {

    /** In-memory data store. */
    val store = mutableMapOf<String, T>()

    override val commonContainer = container
    override val readOnly = false
    override val readOnlyErrorMsg = "Read only"
    override val changeLogCollFun: () -> IChangeLogRepository? = { null }
    override val dependencies: (() -> List<IRepository.Dependency<*, String>>)? = null
    override val userCollFun: () -> IUserRepository<*, String>? = { null }

    override suspend fun insertOne(item: T, apiFilter: FILT, call: ApplicationCall?): ItemState<T> {
        store[item._id] = item
        return ItemState(item = item)
    }

    override suspend fun findById(id: String?, apiFilter: FILT): T? = store[id]

    override suspend fun findItemStateById(id: String?, apiFilter: FILT): ItemState<T> {
        val item = store[id]
        return if (item != null) ItemState(item = item) else ItemState()
    }

    override suspend fun updateOne(item: T, apiFilter: FILT, call: ApplicationCall?): ItemState<T> {
        store[item._id] = item
        return ItemState(item = item)
    }

    override suspend fun deleteOne(id: String, apiFilter: FILT): ItemState<T> {
        val item = store.remove(id)
        return if (item != null) ItemState(item = item) else ItemState(state = State.Error, msgError = "Not found")
    }

    override suspend fun apiItemProcess(call: ApplicationCall?, iApiItem: IApiItem<T, String, FILT>): ItemState<T> =
        ItemState()

    override suspend fun apiListProcess(call: ApplicationCall?, apiList: ApiList<FILT>): ListState<T> {
        val page = apiList.tabPage ?: 1
        val size = apiList.tabSize ?: 25
        val all = store.values.toList()
        val start = (page - 1) * size
        val pageData = all.drop(start).take(size)
        val lastPage = if (all.isEmpty()) 1 else ((all.size - 1) / size) + 1
        return ListState(data = pageData, last_page = lastPage, last_row = all.size)
    }

    override suspend fun findList(apiFilter: FILT): List<T> = store.values.toList()
    override suspend fun findOne(apiFilter: FILT): T? = store.values.firstOrNull()
    override suspend fun getCrudPermission(call: ApplicationCall, crudTask: CrudTask): SimpleState = SimpleState(State.Ok)
    override suspend fun existsByField(property: KProperty1<out BaseDoc<*>, *>, value: Any?): Boolean = false
    override suspend fun findChildrenNot(item: T): ItemState<T> = ItemState(item = item)

    override suspend fun onQueryCreate(apiItem: ApiItem.Query.Create<T, String, FILT>) = SimpleState(State.Ok)
    override suspend fun onQueryCreateItem(apiItem: ApiItem.Query.Create<T, String, FILT>) = ItemState<T>()
    override suspend fun onQueryRead(apiItem: ApiItem.Query.Read<T, String, FILT>) = SimpleState(State.Ok)
    override suspend fun onQueryUpdate(apiItem: ApiItem.Query.Update<T, String, FILT>, orig: T) = SimpleState(State.Ok)
    override suspend fun onQueryDelete(apiItem: ApiItem.Query.Delete<T, String, FILT>, item: T) = SimpleState(State.Ok)
    override suspend fun onQueryUpsert(apiItem: ApiItem.Query<T, String, FILT>, orig: T?) = SimpleState(State.Ok)
    override suspend fun onBeforeCreateAction(apiItem: ApiItem.Action.Create<T, String, FILT>) = ItemState<T>()
    override suspend fun onBeforeUpdateAction(apiItem: ApiItem.Action.Update<T, String, FILT>, orig: T) = ItemState<T>()
    override suspend fun onBeforeDeleteAction(apiItem: ApiItem.Action.Delete<T, String, FILT>) = ItemState<T>()
    override suspend fun onBeforeUpsertAction(apiItem: ApiItem.Action<T, String, FILT>, orig: T?) = ItemState<T>()
    override suspend fun onAfterCreateAction(apiItem: ApiItem.Action.Create<T, String, FILT>, result: Boolean) {}
    override suspend fun onAfterUpdateAction(apiItem: ApiItem.Action.Update<T, String, FILT>, orig: T, result: Boolean) {}
    override suspend fun onAfterDeleteAction(apiItem: ApiItem.Action.Delete<T, String, FILT>, result: Boolean) {}
    override suspend fun onAfterUpsertAction(apiItem: ApiItem.Action<T, String, FILT>, orig: T?, result: Boolean) {}
    override suspend fun onAfterOpen() {}
    override suspend fun onValidate(apiItem: ApiItem.Action<T, String, FILT>, item: T) = SimpleState(State.Ok)
    override suspend fun asApiItem(apiItem: ApiItem<T, String, FILT>) = ItemState(item = apiItem)
}
