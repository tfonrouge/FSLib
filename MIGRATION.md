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
- The `ICommon.name` property is overridden to return the entity class name (e.g.,
  `"Task"`), so URL generation works correctly despite the anonymous object.
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
- Both `apiList` and `apiItem` are `open` — override them to add pre/post
  processing (logging, validation, etc.) without abandoning the base class.
- The `repository` property is `protected`, so subclasses can access it for
  custom queries.
- **Permission checks:** By default, `currentCall()` returns `null`, which skips
  role-based permission checks. Override `currentCall()` in services running
  inside Ktor to enable permission enforcement:

```kotlin
class TaskService(repo: Coll<Task, OId<Task>, ApiFilter, UserId>) :
    StandardCrudService<Task, OId<Task>, ApiFilter>(repo), ITaskService {
    override fun currentCall(): ApplicationCall? = /* from Ktor scope */
}
```

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

**After (reference-based — recommended when views have companion configs):**

```kotlin
import com.fonrouge.fullStack.config.registerEntityViews
import dev.kilua.rpc.getServiceManager

// In App.start():
val reg = registerEntityViews(getServiceManager<ITaskService>()) {
    list(ViewListTask.configViewList, isDefault = true)
    item(ViewItemTask.configViewItem)
}

KVWebManager.initialize {
    defaultView = reg.defaultView
}
```

**After (inline creation — when views don't have companion configs):**

```kotlin
val reg = registerEntityViews(getServiceManager<ITaskService>()) {
    list(ViewListTask::class, CommonTask, ITaskService::apiList, isDefault = true)
    item(ViewItemTask::class, CommonTask, ITaskService::apiItem)
}
```

**For projects with separate item/list service managers** (e.g., Arel pattern):

```kotlin
val reg = registerEntityViews(
    itemServiceManager = getServiceManager<IItemService>(),
    listServiceManager = getServiceManager<IListService>(),
) {
    list(ViewListOrder.configViewList, isDefault = true)
    item(ViewItemOrder.configViewItem)
}
```

**Notes:**

- **Two registration modes:** Pass an existing config instance (reference-based)
  or pass a KClass + container + function (inline creation). Reference-based is
  recommended when view classes already have companion-object configs, to avoid
  double registration.
- The `isDefault = true` parameter marks that view as the default. Access it via
  `reg.defaultView`. Setting `isDefault = true` on multiple views logs a warning
  and uses the last one.
- Calling `registerEntityViews()` multiple times with different service managers
  logs a warning — consolidate into a single call when possible.
- You can register multiple entities in a single block:

```kotlin
val reg = registerEntityViews(getServiceManager<IMyService>()) {
    list(ViewListTask.configViewList, isDefault = true)
    item(ViewItemTask.configViewItem)
    list(ViewListProject.configViewList)
    item(ViewItemProject.configViewItem)
}
```

---

### 4. `simpleCommon()` — non-data views (landing pages, dashboards)

**Package:** `com.fonrouge.base.common`
**Module:** `:core` (commonMain)

For views that don't manage a data model (no `BaseDoc`, no CRUD), use `simpleCommon()`
to create a lightweight `ICommon` instance instead of `ICommonContainer`:

```kotlin
import com.fonrouge.base.common.simpleCommon
import com.fonrouge.fullStack.config.configView
import com.fonrouge.fullStack.view.View

// Lightweight metadata — label and filter only, no data model
val CommonHome = simpleCommon(label = "Home")

// View configuration
val configViewHome = configView(
    viewKClass = ViewHome::class,
    commonContainer = CommonHome,
    baseUrl = "Home",
)

// The view extends View<ApiFilter> directly (not ViewDataContainer)
class ViewHome : View<ApiFilter>(configView = configViewHome) {
    override fun Container.displayPage() {
        h1(content = "Welcome")
    }
}
```

Register non-data views in the DSL with `view()`:

```kotlin
val reg = registerEntityViews(getServiceManager<ITaskService>()) {
    view(ViewHome.configViewHome, isDefault = true)   // non-data landing page
    list(ViewListTask.configViewList)                  // data-bound list
    item(ViewItemTask.configViewItem)                  // data-bound form
}
```

For custom filter types (e.g., dashboard state), use `simpleCommonWithFilter<FILT>()`.

See `samples/fullstack/showcase/.../ViewHome.kt` for a complete example.

---

### Migration checklist

- [ ] Replace `ICommonContainer` object declarations with `simpleContainer()` /
      `simpleContainerWithFilter()` calls
- [ ] Replace pass-through service classes with `StandardCrudService` inheritance
- [ ] If using `StandardCrudService` with Ktor auth, override `currentCall()`
- [ ] Replace manual `ViewRegistry` setup + companion force-references with
      `registerEntityViews()` DSL
- [ ] Verify build: `./gradlew build`
- [ ] Verify runtime: confirm views load and CRUD operations work
