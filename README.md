# FSLib

**[Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) full-stack CRUD library for [MongoDB](https://www.mongodb.com/) and SQL backends with [KVision](https://kvision.io/) frontend.**

FSLib provides a backend-agnostic repository pattern, declarative view configuration, Tabulator-based data grids, role-based access control, change logging, and shared data models across JVM/JS targets. It eliminates repetitive CRUD boilerplate so you can focus on business logic.

---

## Key Features

- **Modular Database Engines** — MongoDB (via [KMongo](https://litote.org/kmongo/)) and SQL (via [Exposed](https://github.com/JetBrains/Exposed)) are independent, optional modules. Use one, the other, or both through a single `IRepository` interface with cross-engine dependency checking.
- **Full-Stack Type Safety** — Shared Kotlin models, serializers, and RPC service definitions between server and browser via [Kilua RPC](https://github.com/rjaros/kilua-rpc).
- **Declarative View System** — Configure list and item views with `ConfigViewList` / `ConfigViewItem`. The framework handles routing, pagination, forms, and CRUD operations.
- **[Tabulator](https://tabulator.info/) Integration** — Server-side pagination, filtering, and sorting out of the box with `TabulatorViewList`.
- **Lifecycle Hooks** — `onQueryCreate`, `onBeforeUpdateAction`, `onAfterDeleteAction`, `onValidate`, and many more hooks on the repository for validation, transformation, and side effects.
- **Role-Based Access Control** — Built-in permission system with users, groups, roles, and per-CRUD-task permissions. Decoupled from any specific database engine via `IRolePermissionProvider`.
- **Change Logging** — Automatic audit trail recording before/after snapshots on create, update, and delete operations.
- **File Attachments** — `DataMedia` support (via the `:media` module) for managing file uploads with thumbnails and metadata.
- **Help Documentation** — Module-scoped contextual help with tutorial and quick-reference HTML pages, auto-discovered per view.
- **Multiple ID Types** — `OId` (MongoDB ObjectId), `IntId`, `LongId`, `StringId` — all with custom serializers.
- **In-Memory Repository** — The `:memorydb` module provides an `InMemoryRepository` for samples, tests, and prototyping without any database engine.
- **Named Routes & API Contract** — The `fslib-named-routes` [Gradle](https://gradle.org/) plugin produces human-readable route paths (`/rpc/ITaskService.apiList`). The `RouteContract` class exposes a `/apiContract` endpoint for third-party client (Android, etc.) route discovery.
- **Server-Side Rendering** — The `:ssr` module provides SSR support using [Ktor](https://ktor.io/) HTML builder.

---

## Architecture

```
your-app  ──>  fullstack  ──>  core
               mongodb    ──>  fullstack, core
               sql        ──>  fullstack, core
               memorydb   ──>  fullstack, core
               media      ──>  fullstack, core, mongodb
               ssr        ──>  fullstack, core, mongodb
```

| Module | Purpose |
|--------|---------|
| **`:core`** | Platform-independent foundation: `BaseDoc<ID>`, ID types, annotations, serializers, state management, user/role models, API framework, date/math utilities. |
| **`:fullstack`** | Core library. **jvmMain**: `IRepository` interface, `IRolePermissionProvider`, `PermissionRegistry`, permissions, change logging, `RouteContract` for API contract discovery, [Ktor](https://ktor.io/) server stack. **jsMain**: View system, configuration, [Tabulator](https://tabulator.info/) wrappers, layout helpers, `ViewRegistry`. **commonMain**: Shared RPC interfaces via [Kilua RPC](https://github.com/rjaros/kilua-rpc). |
| **`:mongodb`** | MongoDB engine (JVM-only). `Coll` implementation with aggregation pipelines, lookups, filtering, change logging, and role-based access via [KMongo](https://litote.org/kmongo/) coroutine driver. |
| **`:sql`** | SQL engine (JVM-only). `SqlRepository` implementation using [Exposed](https://github.com/JetBrains/Exposed) for relational database access with type-aware filtering and identifier quoting. |
| **`:memorydb`** | In-memory database engine (JVM-only). `InMemoryRepository` using `ConcurrentHashMap` for storage. Designed for samples, tests, and prototyping — no database engine required. |
| **`:media`** | Extensions: `DataMedia` (file attachments) and `ChangeLog` views built on top of `:fullstack`. |
| **`:ssr`** | Server-side rendering with [Ktor](https://ktor.io/) HTML builder. |

### Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | [Kotlin](https://kotlinlang.org/) (Multiplatform) | 2.3.x |
| Backend | [Ktor](https://ktor.io/) (Netty) | 3.4.x |
| MongoDB | [KMongo](https://litote.org/kmongo/) (coroutine) | 5.5.x |
| SQL | [Exposed](https://github.com/JetBrains/Exposed) | 0.61.x |
| Frontend | [KVision](https://kvision.io/) | 9.4.x |
| RPC | [Kilua RPC](https://github.com/rjaros/kilua-rpc) | 0.0.42 |
| Serialization | [kotlinx-serialization](https://github.com/Kotlin/kotlinx.serialization) | 1.10.x |
| JVM | Toolchain 21 | |

---

## Installation

FSLib is available on [Maven Central](https://central.sonatype.com/namespace/com.fonrouge.fslib).

### Gradle (Kotlin DSL)

Add the dependency to your module's `build.gradle.kts`:

```kotlin
// Version catalog (gradle/libs.versions.toml)
[versions]
fslib = "3.0.2"

[libraries]
fslib-core = { module = "com.fonrouge.fslib:core", version.ref = "fslib" }
fslib-fullstack = { module = "com.fonrouge.fslib:fullstack", version.ref = "fslib" }
fslib-mongodb = { module = "com.fonrouge.fslib:mongodb", version.ref = "fslib" }
fslib-sql = { module = "com.fonrouge.fslib:sql", version.ref = "fslib" }
fslib-memorydb = { module = "com.fonrouge.fslib:memorydb", version.ref = "fslib" }
fslib-media = { module = "com.fonrouge.fslib:media", version.ref = "fslib" }
fslib-ssr = { module = "com.fonrouge.fslib:ssr", version.ref = "fslib" }
```

```kotlin
// build.gradle.kts — In-memory (prototyping/samples, no database required)
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api("com.fonrouge.fslib:fullstack:3.0.2")
            }
        }
        jvmMain {
            dependencies {
                implementation("com.fonrouge.fslib:memorydb:3.0.2")
            }
        }
    }
}
```

```kotlin
// build.gradle.kts — MongoDB application
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api("com.fonrouge.fslib:fullstack:3.0.2")
            }
        }
        jvmMain {
            dependencies {
                implementation("com.fonrouge.fslib:mongodb:3.0.2")
            }
        }
    }
}
```

```kotlin
// build.gradle.kts — SQL application
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api("com.fonrouge.fslib:fullstack:3.0.2")
            }
        }
        jvmMain {
            dependencies {
                implementation("com.fonrouge.fslib:sql:3.0.2")
            }
        }
    }
}
```

```kotlin
// build.gradle.kts — Hybrid (MongoDB + SQL)
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api("com.fonrouge.fslib:fullstack:3.0.2")
            }
        }
        jvmMain {
            dependencies {
                implementation("com.fonrouge.fslib:mongodb:3.0.2")
                implementation("com.fonrouge.fslib:sql:3.0.2")
            }
        }
    }
}
```

### Publishing to Local Maven

```bash
./gradlew publishToMavenLocal
```

This publishes `:core`, `:fullstack`, `:mongodb`, `:sql`, `:memorydb`, `:media`, and `:ssr` to your local Maven repository (`~/.m2/repository`).

---

## Quick Start

### 1. Define a Model (commonMain)

```kotlin
@Serializable
@Collection("customers")
data class Customer(
    override val _id: OId<Customer> = OId(),
    val name: String = "",
    val email: String = "",
    val active: Boolean = true,
) : BaseDoc<OId<Customer>>
```

### 2. Define a Common Container (commonMain)

```kotlin
object CommonCustomer : ICommonContainer<Customer, OId<Customer>, CustomerFilter> {
    override val itemKClass = Customer::class
    override val idSerializer = OIdSerializer
    override val apiFilterSerializer = CustomerFilter.serializer()
    override val labelItem = "Customer"
    override val labelList = "Customers"
    override val labelId: (Customer?) -> String = { it?.name ?: "" }
}
```

### 3. Define an RPC Service (commonMain)

```kotlin
@KiluaRpcServiceName("ICustomerService")
interface ICustomerService {
    suspend fun apiItem(iApiItem: IApiItem<Customer, OId<Customer>, CustomerFilter>): ItemState<Customer>
    suspend fun apiList(apiList: ApiList<CustomerFilter>): ListState<Customer>
}
```

### 4. Implement the Repository (jvmMain — MongoDB)

```kotlin
class CustomerColl : Coll<CommonCustomer, Customer, OId<Customer>, CustomerFilter, OId<User>>(
    commonContainer = CommonCustomer,
    mongoDatabase = MongoDb.database,
) {
    override fun findItemFilter(apiFilter: CustomerFilter): Bson? {
        // Custom filtering logic
        return apiFilter.nameSearch?.let {
            Customer::name regex Regex(it, RegexOption.IGNORE_CASE)
        }
    }
}
```

### 5. Implement the Repository (jvmMain — SQL Alternative)

```kotlin
class CustomerSqlRepo : SqlRepository<CommonCustomer, Customer, OId<Customer>, CustomerFilter, OId<User>>(
    commonContainer = CommonCustomer,
    sqlDatabase = mySqlDatabase,
) {
    override fun buildWhereFromApiFilter(
        apiFilter: CustomerFilter,
        whereClauses: MutableList<String>,
        whereArgs: MutableList<Pair<IColumnType<*>, Any?>>,
    ) {
        apiFilter.nameSearch?.let {
            whereClauses += "name LIKE ?"
            whereArgs += VarCharColumnType() to "%$it%"
        }
    }
}
```

### 6. Configure Views (jsMain)

```kotlin
// List view configuration
object ConfigViewListCustomer : ConfigViewList<
    CommonCustomer, Customer, OId<Customer>,
    ViewListCustomer, CustomerFilter, Unit, ICustomerService
>(
    commonContainer = CommonCustomer,
    viewKClass = ViewListCustomer::class,
    apiListFun = ICustomerService::apiList,
    serviceManager = ViewRegistry.listServiceManager,
)

// Item view configuration
object ConfigViewItemCustomer : ConfigViewItem<
    CommonCustomer, Customer, OId<Customer>,
    ViewItemCustomer, CustomerFilter, ICustomerService
>(
    commonContainer = CommonCustomer,
    viewKClass = ViewItemCustomer::class,
    apiItemFun = ICustomerService::apiItem,
    serviceManager = ViewRegistry.itemServiceManager,
)
```

### 7. Implement Views (jsMain)

```kotlin
class ViewListCustomer : ViewList<CommonCustomer, Customer, OId<Customer>, CustomerFilter, Unit>() {
    override val configView = ConfigViewListCustomer

    override fun Container.displayPage() {
        fsTabulator(viewList = this@ViewListCustomer) {
            addColumn("Name") { it.name }
            addColumn("Email") { it.email }
            addColumn("Active") { if (it.active) "Yes" else "No" }
        }
    }
}

class ViewItemCustomer : ViewItem<CommonCustomer, Customer, OId<Customer>, CustomerFilter>() {
    override val configView = ConfigViewItemCustomer

    override fun Container.displayPage() {
        formPanel = ViewFormPanel.xcreate(viewItem = this@ViewItemCustomer) {
            formRow {
                text(label = "Name", value = Customer::name)
                text(label = "Email", value = Customer::email)
            }
        }
    }
}
```

---

## Repository Lifecycle Hooks

The `IRepository` interface provides hooks at every stage of CRUD operations:

```
Query Phase (validation)          Action Phase (mutation)
─────────────────────            ──────────────────────
onQueryCreate                    onBeforeCreateAction  →  DB INSERT  →  onAfterCreateAction
onQueryRead
onQueryUpdate                    onBeforeUpdateAction  →  DB UPDATE  →  onAfterUpdateAction
onQueryDelete                    onBeforeDeleteAction  →  DB DELETE  →  onAfterDeleteAction
onQueryCreateItem                onBeforeUpsertAction (shared create/update)
onQueryUpsert (shared)           onAfterUpsertAction  (shared create/update)
                                 onValidate (content validation)
```

Override any hook in your repository class:

```kotlin
class CustomerColl : Coll<...>(...) {
    override suspend fun onValidate(apiItem: ApiItem<...>, item: Customer): SimpleState {
        if (item.email.isBlank()) return simpleErrorState("Email is required")
        return SimpleState(true)
    }

    override suspend fun onBeforeCreateAction(apiItem: ApiItem.Action.Create<...>): ItemState<Customer> {
        // Transform item before insert
        return ItemState(item = apiItem.item.copy(name = apiItem.item.name.trim()))
    }

    override suspend fun onAfterCreateAction(apiItem: ApiItem.Action.Create<...>, itemState: ItemState<Customer>) {
        // Side effects after insert (send email, update cache, etc.)
    }
}
```

---

## SQL Annotations

Located in `com.fonrouge.base.annotations`:

| Annotation | Target | Purpose |
|-----------|--------|---------|
| `@Collection(name)` | Class | Maps class to MongoDB collection or SQL table name |
| `@SqlField(name, compound)` | Property | Maps property to a specific SQL column name or marks it as a compound (nested) field |
| `@SqlIgnoreField` | Property | Excludes property from SQL INSERT/UPDATE statements |
| `@SqlOneToOne` | Property | Marks a one-to-one relationship for SQL mapping |
| `@PreLookupField` | Property | Indicates a pre-lookup field for initial filtering |

---

## Role-Based Access Control

FSLib includes a built-in RBAC system:

- **`IAppRole`** — Defines available roles (per class, per CRUD task)
- **`IRoleInUser`** — Assigns roles to individual users (Allow / Deny / Default)
- **`IGroupOfUser`** — Groups users together
- **`IRoleInGroup`** — Assigns roles to groups
- **`IUserGroup`** — Links users to groups with inherited roles

Permissions are checked automatically on every CRUD operation via `getCrudPermission()`. Roles are auto-created for new repository classes on first access.

The permission system is decoupled from the database engine through `IRolePermissionProvider` and `PermissionRegistry`. The MongoDB module registers its provider automatically; SQL repositories consume it without importing MongoDB types.

---

## Change Logging

Enable audit trails by providing a `changeLogCollFun` on your repository:

```kotlin
class CustomerColl : Coll<...>(...) {
    override val changeLogCollFun = { ChangeLogColl() }
}
```

Every create, update, and delete operation automatically records:
- Action type (Create / Update / Delete)
- Timestamp
- User ID and info
- Before/after field values (for updates)
- Client info

View change logs with the `IViewListChangeLog` interface from the `:media` module.

---

## File Attachments (DataMedia)

The `:media` module provides file attachment support:

```kotlin
// Define your DataMedia model implementing IDataMedia<User, OId<User>>
// Use IDataMediaColl for the MongoDB collection
// Use IViewListDataMedia for the frontend view with upload, thumbnail preview, and download
```

Features: file upload with type filtering, thumbnail generation, ordering, metadata tracking (size, content type, user, date).

---

## Help Documentation

FSLib supports module-scoped contextual help. See `HELP-DOCS-GUIDE.md` for the complete guide.

**Directory structure:**
```
help-docs/
  ViewListCustomer/
    tutorial.html     # Step-by-step guide
    context.html      # Quick reference
  ViewItemCustomer/
    tutorial.html
    context.html
```

Help buttons appear automatically when documentation files exist for a view.

---

## Named Routes & API Contract

FSLib includes a system for exposing RPC endpoints to third-party clients (Android, native, etc.) that don't use KSP-generated Kilua RPC proxies.

### Named Routes (Gradle Plugin)

The `fslib-named-routes` convention plugin post-processes KSP-generated `ServiceManager` code, replacing counter-based route names with explicit `"ServiceName.methodName"` strings:

```kotlin
// build.gradle.kts
plugins {
    id("fslib-named-routes")  // Apply alongside kilua.rpc
}
```

This transforms routes from `/rpc/routeTaskServiceManager0` to `/rpc/ITaskService.apiList` — human-readable, self-documenting, and order-independent.

### API Contract Endpoint

`RouteContract` reads actual routes from Kilua RPC's registry and serves them at `/apiContract`:

```kotlin
// Main.kt (jvmMain)
val contract = RouteContract(version = "1.0.0")
contract.register(TaskServiceManager, "ITaskService")

routing {
    apiContractEndpoint(contract)
}
```

Third-party clients fetch the contract at startup to discover available services:

```json
{
  "version": "1.0.0",
  "protocol": {
    "format": "json-rpc-2.0",
    "contentType": "application/json",
    "paramEncoding": "each parameter is individually JSON-serialized into a string element of the params array",
    "resultEncoding": "the result field contains a JSON-serialized string that must be deserialized a second time"
  },
  "services": [
    {
      "service": "ITaskService",
      "methods": {
        "apiList": { "route": "/rpc/ITaskService.apiList", "method": "POST" },
        "apiItem": { "route": "/rpc/ITaskService.apiItem", "method": "POST" }
      }
    }
  ]
}
```

### Shared Contract Library

For compile-time type safety between server and client, split your models and service contract into a shared library module:

```kotlin
// showcase-lib (shared, no server/frontend dependencies)
interface ITaskServiceContract {
    suspend fun apiList(apiList: ApiList<TaskFilter>): ListState<Task>
    suspend fun apiItem(iApiItem: IApiItem<Task, String, TaskFilter>): ItemState<Task>
}

// showcase-app (server) — extends the contract with @RpcService
@RpcService
interface ITaskService : ITaskServiceContract { ... }

// Android client — implements the contract with HTTP calls
class ITaskService : ITaskServiceContract {
    override suspend fun apiList(apiList: ApiList<TaskFilter>): ListState<Task> =
        call("apiList", apiList)
}
```

See `samples/fullstack/showcase/` for a complete working example with `showcase-lib` and `showcase-app`.

---

## Build Commands

```bash
./gradlew build                    # Build all modules
./gradlew :core:build              # Build only the core module
./gradlew :fullstack:build         # Build only the fullstack module
./gradlew :mongodb:build           # Build only the mongodb module
./gradlew :sql:build               # Build only the sql module
./gradlew :media:build             # Build only the media module
./gradlew :ssr:build               # Build only the ssr module
./gradlew publishToMavenLocal      # Publish to local Maven
```

### Sample Applications

```bash
# Fullstack samples (KVision + Ktor)
./gradlew :samples:fullstack:rpc-demo:run          # RPC demo
./gradlew :samples:fullstack:greeting:run           # Simple greeting
./gradlew :samples:fullstack:contacts:run           # Contacts grid
./gradlew :samples:fullstack:showcase:showcase-app:run  # Showcase (InMemoryRepository + API contract)

# SSR samples (Ktor HTML builder)
./gradlew :samples:ssr:basic:run
./gradlew :samples:ssr:catalog:run
./gradlew :samples:ssr:advanced:run
```

---

## Migration

- **From 1.x → 2.0:** See `MIGRATION-GUIDE-2.0.md`
- **From 2.0 → 3.0:** See `MIGRATION-GUIDE-3.0.md` — covers module renames, engine extraction, permission decoupling, and dependency changes.

---

## Project Structure

```
FSLib/
  core/                            # :core module (formerly :base)
    src/
      commonMain/                  # BaseDoc, ID types, annotations, serializers, state, API
      jvmMain/                     # BSON serializers, JVM utilities
      jsMain/                      # Browser utilities, JS serializers
  fullstack/                       # :fullstack module (formerly :fullStack)
    src/
      commonMain/                  # Shared RPC interfaces
      jvmMain/                     # IRepository, IRolePermissionProvider, PermissionRegistry
      jsMain/                      # Views, config, Tabulator, layout helpers
  mongodb/                         # :mongodb module (JVM-only)
    src/main/kotlin/               # Coll, aggregation pipelines, BSON helpers
  sql/                             # :sql module (JVM-only)
    src/main/kotlin/               # SqlRepository, SqlDatabase
  memorydb/                        # :memorydb module (JVM-only)
    src/main/kotlin/               # InMemoryRepository
  media/                           # :media module (formerly :utils)
    src/
      commonMain/                  # DataMedia, ChangeLog interfaces
      jvmMain/                     # DataMedia MongoDB collection
      jsMain/                      # DataMedia and ChangeLog views
  ssr/                             # :ssr module
    src/main/kotlin/               # Server-side rendering with Ktor HTML builder
  buildSrc/                        # Gradle convention plugins
    src/main/kotlin/
      fslib-publishing.gradle.kts  # Maven Central publishing
      fslib-named-routes.gradle.kts # Named routes for Kilua RPC
  samples/                         # Sample applications
    fullstack/
      rpc-demo/                    # Full-stack KVision + Ktor sample
      greeting/                    # Simple greeting sample
      contacts/                    # Contacts sample
      showcase/
        showcase-lib/              # Shared models + contract (publishable)
        showcase-app/              # Full-stack app with API contract endpoint
    ssr/
      basic/                       # Basic SSR sample
      catalog/                     # Catalog SSR sample
      advanced/                    # Advanced SSR sample
  CLAUDE.md                        # AI assistant instructions
  HELP-DOCS-GUIDE.md               # Help documentation guide
  MIGRATION-GUIDE-2.0.md           # 1.x → 2.0 migration guide
  MIGRATION-GUIDE-3.0.md           # 2.0 → 3.0 migration guide
```

---

## Requirements

- **JDK 21** or higher
- **MongoDB** (if using the `:mongodb` module)
- **SQL Server** (if using the `:sql` module — MSSQL via jTDS or JDBC driver)
- **Chrome** (for JS tests via Karma)

---

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes with KDoc comments on all public APIs
4. Run `./gradlew build` to verify
5. Submit a pull request

---

## License

See the project repository for license information.
