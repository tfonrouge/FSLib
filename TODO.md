# TODO: Improve fsLib Data Structures

---

## Analysis: Data Structures Required Per Entity

For **one entity** (e.g., `Task`), a developer must currently define:

| # | What | Where | Purpose |
|---|------|-------|---------|
| 1 | `Task` data class implementing `BaseDoc<String>` | commonMain | The model |
| 2 | `CommonTask` object extending `ICommonContainer<Task, String, ApiFilter>` | commonMain | Metadata container (KClass, labels) |
| 3 | `ITaskService` interface with `@RpcService` | commonMain | RPC contract |
| 4 | `TaskService` class implementing `ITaskService` | jvmMain | Backend (delegates to repository) |
| 5 | Repository instance (`Coll`, `InMemoryRepository`, etc.) | jvmMain | Storage |
| 6 | `ViewListTask` extending `ViewList<Task, String, ApiFilter, Unit>` | jsMain | List view |
| 7 | `ViewItemTask` extending `ViewItem<Task, String, ApiFilter>` | jsMain | Form view |

*Custom filter class only needed when the entity has domain-specific filtering (use `ApiFilter` otherwise).*

### Identified issues (prioritized)

#### A. ICommonContainer carries too many responsibilities
It bundles **metadata** (KClass, serializers), **display labels** (labelId, labelItem, labelList), and **API item factory methods** (7 factory functions + toIApiItem). These are three distinct concerns. You can't use the model without pulling in UI labels.

#### B. ~~Generic parameter explosion~~ (resolved)
~~A single `Coll` declaration carried **5 type parameters**: `<CC, T, ID, FILT, UID>`. `CC` already encoded `T`, `ID`, and `FILT` — yet all four had to be repeated.~~ Now reduced to `<T, ID, FILT, UID>` — CC removed in v3.1.0. `UID` still rarely varies per entity (potential future improvement).

#### C. BaseDoc forces `_id` property name
The `_id` convention is MongoDB-specific. SQL entities typically use `id`. The `@Suppress("PropertyName")` acknowledges this. However, changing it would be a massive breaking change with minimal practical benefit.

#### D. ICommon / ICommonContainer split is unclear
`ICommon` holds `label`, `apiFilterSerializer`, and `apiFilterInstance()`. Almost every view uses `ICommonContainer`. The two-level hierarchy adds type parameter complexity that ripples through the entire generic chain.

#### E. Coll is massive (1693 lines) with mixed abstraction levels
It handles CRUD, aggregation pipelines, lookups, pagination, permission checks, change logging, error formatting, and reflection-based copying — all in one class.

### Improvement roadmap

| Priority | Suggestion | Effort | Impact | Status |
|----------|-----------|--------|--------|--------|
| 1 | ~~Derive serializers from KClass~~ | Low | High | **Done** (v3.1.0) — `idSerializer` derived from `GeneratedSerializer.childSerializers()`, `apiFilterSerializer` derived from `filterKClass` |
| 2 | ~~Default labels from KClass.simpleName~~ | Low | Medium | **Already done** — defaults exist in `ICommonContainer` |
| 3 | ~~Default filter type~~ | Medium | Medium | **Done** (v3.1.0) — use `ApiFilter` directly, no empty filter classes needed |
| 4 | ~~Remove CC type parameter~~ | Medium | High | **Done** (v3.1.0) — CC removed from all generic chains (`Coll<T,ID,FILT,UID>`, `ViewList<T,ID,FILT,MID>`, etc.). `commonContainer` property typed as `ICommonContainer<T,ID,FILT>` directly. |
| 5 | **Separate labels from container** | Low | Medium — cleaner separation of concerns |
| 6 | **Entity registration DSL** | High | High — consolidates scattered pieces into one site |
