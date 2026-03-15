# TODO: Improve fsLib Data Structures

---

## Analysis: Data Structures Required Per Entity

For **one entity** (e.g., `Task`), a developer must currently define:

| # | What | Where | Purpose |
|---|------|-------|---------|
| 1 | `Task` data class implementing `BaseDoc<String>` | commonMain | The model |
| 2 | `CommonTask` via `simpleContainer<Task, String>(...)` | commonMain | Metadata container (KClass, labels) |
| 3 | `ITaskService` interface with `@RpcService` | commonMain | RPC contract |
| 4 | `TaskService` extending `StandardCrudService` | jvmMain | Backend (delegates to repository) |
| 5 | Repository instance (`Coll`, `InMemoryRepository`, etc.) | jvmMain | Storage |
| 6 | `ViewListTask` extending `ViewList<Task, String, ApiFilter, Unit>` | jsMain | List view |
| 7 | `ViewItemTask` extending `ViewItem<Task, String, ApiFilter>` | jsMain | Form view |

*Custom filter class only needed when the entity has domain-specific filtering (use `ApiFilter` otherwise).*

### Identified issues

#### A. ICommonContainer carries too many responsibilities
It bundles **metadata** (KClass, serializers), **display labels** (labelId, labelItem, labelList), and **API item factory methods** (7 factory functions + toIApiItem). These are three distinct concerns. You can't use the model without pulling in UI labels. Separating labels was considered (Priority 5 in the original roadmap) but deemed not worth the added complexity — labels have good defaults via `simpleContainer()` and are always needed where the container is used.

#### B. ICommon / ICommonContainer split
`ICommon` holds `label`, `apiFilterSerializer`, and `apiFilterInstance()`. Almost every view uses `ICommonContainer`. The two-level hierarchy adds type parameter complexity. **However**, the split is intentional: `ConfigView` accepts `ICommon<FILT>` (not `ICommonContainer`), enabling lightweight non-data views (landing pages, dashboards, settings) that use the filter as state without needing a data model. The showcase sample demonstrates this with `ViewHome`. This is a design choice, not an issue.

#### C. BaseDoc forces `_id` property name
The `_id` convention is MongoDB-specific. SQL entities typically use `id`. The `@Suppress("PropertyName")` acknowledges this. However, changing it would be a massive breaking change with minimal practical benefit.

#### D. Coll is large (~1700 lines) with mixed abstraction levels
It handles CRUD, aggregation pipelines, lookups, pagination, permission checks, change logging, error formatting, and reflection-based copying — all in one class. Analysis shows:
- **Extractable:** Pipeline building (~230 lines) into `AggregationPipelineBuilder`, item copying (~60 lines) into a utility, error formatting (~20 lines).
- **Not easily extractable:** Lifecycle hooks (must remain `open` for subclass overrides), CRUD orchestration (tightly coupled to hooks + permissions + changelog), IRepository bridge methods (interface contract).
- **Verdict:** Extracting the pipeline builder is the highest-value refactor (~230 lines, cohesive responsibility, reusable). Beyond that, returns diminish because remaining methods are tightly coupled to the lifecycle orchestration.
