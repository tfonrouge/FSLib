# FSLib

**Kotlin Multiplatform full-stack CRUD library for MongoDB and SQL backends with KVision frontend.**

FSLib provides a backend-agnostic repository pattern, declarative view configuration, Tabulator-based data grids, role-based access control, change logging, and shared data models across JVM/JS targets. It eliminates repetitive CRUD boilerplate so you can focus on business logic.

---

## Key Features

- **Dual Database Engine** — Use MongoDB (via KMongo) and/or SQL (via Exposed) through a single `IRepository` interface. Mix engines in the same application with cross-engine dependency checking.
- **Full-Stack Type Safety** — Shared Kotlin models, serializers, and RPC service definitions between server and browser via Kilua RPC.
- **Declarative View System** — Configure list and item views with `ConfigViewList` / `ConfigViewItem`. The framework handles routing, pagination, forms, and CRUD operations.
- **Tabulator Integration** — Server-side pagination, filtering, and sorting out of the box with `TabulatorViewList`.
- **Lifecycle Hooks** — `onQueryCreate`, `onBeforeUpdateAction`, `onAfterDeleteAction`, `onValidate`, and many more hooks on the repository for validation, transformation, and side effects.
- **Role-Based Access Control** — Built-in permission system with users, groups, roles, and per-CRUD-task permissions.
- **Change Logging** — Automatic audit trail recording before/after snapshots on create, update, and delete operations.
- **File Attachments** — `DataMedia` support (via the `:utils` module) for managing file uploads with thumbnails and metadata.
- **Help Documentation** — Module-scoped contextual help with tutorial and quick-reference HTML pages, auto-discovered per view.
- **Multiple ID Types** — `OId` (MongoDB ObjectId), `IntId`, `LongId`, `StringId` — all with custom serializers.

---

## Architecture

```
your-app  ──>  fullStack  ──>  base
               utils  ────>  base
```

| Module | Purpose |
|--------|---------|
| **`:base`** | Platform-independent foundation: `BaseDoc<ID>`, ID types, annotations, serializers, state management, user/role models, API framework, `SqlDatabase`, date/math utilities. |
| **`:fullStack`** | Core library. **jvmMain**: `IRepository`, `Coll` (MongoDB), `SqlRepository` (SQL), permissions, change logging. **jsMain**: View system, configuration, Tabulator wrappers, layout helpers, `ViewRegistry`. **commonMain**: Shared RPC interfaces. |
| **`:utils`** | Extensions: `DataMedia` (file attachments) and `ChangeLog` views built on top of `:fullStack`. |

### Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Kotlin (Multiplatform) | 2.3.x |
| Backend | Ktor (Netty) | 3.4.x |
| MongoDB | KMongo (coroutine) | 5.5.x |
| SQL | Exposed | 0.61.x |
| Frontend | KVision | 9.4.x |
| RPC | Kilua RPC | 0.0.42 |
| Serialization | kotlinx-serialization | 1.10.x |
| JVM | Toolchain 21 | |

---

## Installation

### Gradle (Kotlin DSL)

Add the dependency to your module's `build.gradle.kts`:

```kotlin
// Version catalog (gradle/libs.versions.toml)
[versions]
fslib = "2.0.0"

[libraries]
fslib-base = { module = "com.fonrouge.fsLib:base", version.ref = "fslib" }
fslib-fullstack = { module = "com.fonrouge.fsLib:fullStack", version.ref = "fslib" }
fslib-utils = { module = "com.fonrouge.fsLib:utils", version.ref = "fslib" }
```

```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api("com.fonrouge.fsLib:fullStack:2.0.0")
                // Optional: file attachments and change log views
                api("com.fonrouge.fsLib:utils:2.0.0")
            }
        }
    }
}
```

### Publishing to Local Maven

```bash
./gradlew publishToMavenLocal
```

This publishes `:base`, `:fullStack`, and `:utils` to your local Maven repository (`~/.m2/repository`).

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

View change logs with the `IViewListChangeLog` interface from the `:utils` module.

---

## File Attachments (DataMedia)

The `:utils` module provides file attachment support:

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

## Build Commands

```bash
./gradlew build                    # Build all modules
./gradlew :base:build              # Build only the base module
./gradlew :fullStack:build         # Build only the fullStack module
./gradlew :utils:build             # Build only the utils module
./gradlew publishToMavenLocal      # Publish to local Maven
```

### Sample Application

```bash
./gradlew :test1:jvmRun            # Run the Ktor backend server
./gradlew :test1:jsRun             # Run the JS dev server
./gradlew :test1:jvmTest           # Run JVM tests
./gradlew :test1:jsTest            # Run JS tests (Chrome headless)
```

---

## Migration from 1.x

See `MIGRATION-GUIDE-2.0.md` for a complete guide covering:
- Breaking changes (import paths, override modifiers, return type updates)
- New features (dual database engine, `SqlRepository`, cross-engine dependencies)
- Step-by-step migration checklist

---

## Project Structure

```
FSLib/
  base/                          # :base module
    src/
      commonMain/                # BaseDoc, ID types, annotations, serializers, state, API
      jvmMain/                   # SqlDatabase, JVM serializers
      jsMain/                    # Browser utilities, JS serializers
  fullStack/                     # :fullStack module
    src/
      commonMain/                # Shared RPC interfaces
      jvmMain/                   # IRepository, Coll, SqlRepository, permissions
      jsMain/                    # Views, config, Tabulator, layout helpers
  utils/                         # :utils module
    src/
      commonMain/                # DataMedia, ChangeLog interfaces
      jvmMain/                   # DataMedia MongoDB collection
      jsMain/                    # DataMedia and ChangeLog views
  test1/                         # Sample application
  CLAUDE.md                      # AI assistant instructions
  HELP-DOCS-GUIDE.md             # Help documentation guide
  MIGRATION-GUIDE-2.0.md         # 1.x → 2.0 migration guide
```

---

## Requirements

- **JDK 21** or higher
- **MongoDB** (if using MongoDB backend)
- **SQL Server** (if using SQL backend — MSSQL via jTDS or JDBC driver)
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
