# Migration Guide

## Entity Registration DSL

This release introduces three convenience APIs that reduce per-entity boilerplate:
`simpleContainer()`, `StandardCrudService`, and `registerEntityViews()`.

All three are **opt-in** — existing code continues to work without changes.

---

### 1. `simpleContainer()` — shorter CommonContainer declarations

**Package:** `com.fonrouge.base.common`

Replaces verbose `object ... : ICommonContainer(...)` declarations by inferring
`itemKClass` and `filterKClass` from reified generics.

**Before:**

```kotlin
import com.fonrouge.base.api.ApiFilter
import com.fonrouge.base.common.ICommonContainer

object CommonTask : ICommonContainer<Task, String, ApiFilter>(
    itemKClass = Task::class,
    filterKClass = ApiFilter::class,
    labelItem = "Task",
    labelList = "Tasks",
    labelId = { it?.let { "${it.title} (${it._id})" } ?: "<no-task>" },
)
```

**After:**

```kotlin
import com.fonrouge.base.common.simpleContainer

val CommonTask = simpleContainer<Task, String>(
    labelItem = "Task",
    labelList = "Tasks",
    labelId = { it?.let { "${it.title} (${it._id})" } ?: "<no-task>" },
)
```

**Notes:**

- `simpleContainer<T, ID>()` uses `ApiFilter` by default. For custom filter types,
  use `simpleContainerWithFilter<T, ID, FILT>()`.
- The result is a `val` instead of an `object`. All existing call sites that reference
  the container by name continue to work — only the declaration changes.
- All parameters (`labelItem`, `labelList`, `labelId`, `labelItemId`) have sensible
  defaults, so you only need to specify what you want to customize.

---

### 2. `StandardCrudService` — eliminate service delegation boilerplate

**Package:** `com.fonrouge.fullStack.services`
**Module:** `:fullstack` (jvmMain)

Replaces manual `apiList` / `apiItem` method implementations that simply forward
to the repository.

**Before:**

```kotlin
import com.fonrouge.base.api.ApiFilter
import com.fonrouge.base.api.ApiList
import com.fonrouge.base.api.IApiItem
import com.fonrouge.base.state.ItemState
import com.fonrouge.base.state.ListState
import com.fonrouge.fullStack.memoryDb.InMemoryRepository

class TaskService(
    private val repo: InMemoryRepository<Task, String, ApiFilter, String>,
) : ITaskService {
    override suspend fun apiList(apiList: ApiList<ApiFilter>): ListState<Task> =
        repo.apiListProcess(apiList = apiList)
    override suspend fun apiItem(iApiItem: IApiItem<Task, String, ApiFilter>): ItemState<Task> =
        repo.apiItemProcess(call = null, iApiItem = iApiItem)
}
```

**After:**

```kotlin
import com.fonrouge.base.api.ApiFilter
import com.fonrouge.fullStack.memoryDb.InMemoryRepository
import com.fonrouge.fullStack.services.StandardCrudService

class TaskService(
    repo: InMemoryRepository<Task, String, ApiFilter, String>,
) : StandardCrudService<Task, String, ApiFilter>(repo), ITaskService
```

**Notes:**

- Works with any `IRepository` implementation (`Coll`, `SqlRepository`,
  `InMemoryRepository`).
- If your service adds custom methods beyond `apiList`/`apiItem`, extend
  `StandardCrudService` and add them in the body — the base CRUD delegation
  is still inherited.
- The `repository` property is `protected`, so subclasses can access it for
  custom queries.

---

### 3. `registerEntityViews()` — declarative view registration

**Package:** `com.fonrouge.fullStack.config`
**Module:** `:fullstack` (jsMain)

Replaces manual `ViewRegistry` setup and force-referenced companion objects with
a single DSL block.

**Before:**

```kotlin
import com.fonrouge.fullStack.config.ViewRegistry
import dev.kilua.rpc.getServiceManager

// In App.start():
val serviceManager = getServiceManager<ITaskService>()
ViewRegistry.itemServiceManager = serviceManager
ViewRegistry.listServiceManager = serviceManager

// Force-reference companions so ConfigView registrations execute
ViewListTask.configViewList
ViewItemTask.configViewItem

KVWebManager.initialize {
    defaultView = ViewListTask.configViewList
}
```

**After:**

```kotlin
import com.fonrouge.fullStack.config.registerEntityViews
import dev.kilua.rpc.getServiceManager

// In App.start():
val reg = registerEntityViews(getServiceManager<ITaskService>()) {
    list(ViewListTask::class, CommonTask, ITaskService::apiList, isDefault = true)
    item(ViewItemTask::class, CommonTask, ITaskService::apiItem)
}

KVWebManager.initialize {
    defaultView = reg.defaultView
}
```

**For projects with separate item/list service managers** (e.g., Arel pattern):

```kotlin
val reg = registerEntityViews(
    itemServiceManager = getServiceManager<IItemService>(),
    listServiceManager = getServiceManager<IListService>(),
) {
    list(ViewListOrder::class, CommonOrder, IListService::apiList, isDefault = true)
    item(ViewItemOrder::class, CommonOrder, IItemService::apiItem)
}
```

**Notes:**

- The `isDefault = true` parameter on either `list()` or `item()` marks that view
  as the default. Access it via `reg.defaultView`.
- Existing companion objects in your `ViewList` / `ViewItem` subclasses can remain
  for backward compatibility — they won't conflict with the DSL registrations.
- You can register multiple entities in a single block if they share the same
  service manager:

```kotlin
val reg = registerEntityViews(getServiceManager<IMyService>()) {
    list(ViewListTask::class, CommonTask, IMyService::apiListTask, isDefault = true)
    item(ViewItemTask::class, CommonTask, IMyService::apiItemTask)
    list(ViewListProject::class, CommonProject, IMyService::apiListProject)
    item(ViewItemProject::class, CommonProject, IMyService::apiItemProject)
}
```

---

### Migration checklist

- [ ] Replace `ICommonContainer` object declarations with `simpleContainer()` /
      `simpleContainerWithFilter()` calls
- [ ] Replace pass-through service classes with `StandardCrudService` inheritance
- [ ] Replace manual `ViewRegistry` setup + companion force-references with
      `registerEntityViews()` DSL
- [ ] Verify build: `./gradlew build`
- [ ] Verify runtime: confirm views load and CRUD operations work
