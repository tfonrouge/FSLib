# FSLib 2.0 Migration Guide

This guide covers all breaking changes, new features, and migration steps when upgrading from FSLib 1.x to 2.0.

---

## Overview

FSLib 2.0 introduces **dual database engine support**. The library now defines a backend-agnostic
`IRepository` interface that both the existing MongoDB `Coll` and the new `SqlRepository` implement.
This enables applications to use MongoDB, SQL (via Exposed), or both engines simultaneously.

### New packages and files

| File | Description |
|------|-------------|
| `repository/IRepository.kt` | Backend-agnostic repository interface (CRUD, lists, hooks, permissions, dependencies) |
| `repository/SqlRepository.kt` | SQL implementation using Exposed + Kotlinx Serialization |
| `repository/IUserRepository.kt` | Backend-agnostic interface for user lookup and session extraction |
| `repository/IChangeLogRepository.kt` | Backend-agnostic interface for audit/change log recording |

All new files are in the `com.fonrouge.fullStack.repository` package under `fullStack/src/jvmMain/`.

---

## Breaking Changes

### 1. `Coll.Dependency` moved to `IRepository.Dependency`

**Before (1.x):**
```kotlin
import com.fonrouge.fullStack.mongoDb.Coll

override val dependencies = {
    listOf(
        Coll.Dependency(
            common = OrderCommon,
            property = Order::customerId,
        )
    )
}
```

**After (2.0):**
```kotlin
import com.fonrouge.fullStack.repository.IRepository

override val dependencies = {
    listOf(
        IRepository.Dependency(
            common = OrderCommon,
            property = Order::customerId,
        )
    )
}
```

**Migration:** Find-and-replace `Coll.Dependency` with `IRepository.Dependency` and update the import.
The constructor parameters are the same, with an optional new `repositoryFun` parameter (see
[Cross-Engine Dependencies](#cross-engine-dependencies) below).

### 2. `Coll` now implements `IRepository`

`Coll` class declaration changed from:
```kotlin
abstract class Coll<CC, T, ID, FILT, UID>(...) {
```
to:
```kotlin
abstract class Coll<CC, T, ID, FILT, UID>(...) : IRepository<CC, T, ID, FILT, UID> {
```

**Impact:** If your code subclasses `Coll` and overrides any of the methods that are now part of
`IRepository`, you may need to add the `override` modifier. The compiler will tell you which ones.
The most commonly affected members:

| Member | Was | Now |
|--------|-----|-----|
| `commonContainer` | `val` | `override val` |
| `readOnly` | `open val` | `override val` |
| `readOnlyErrorMsg` | `val` | `override val` |
| `changeLogCollFun` | `open val` | `override val` |
| `dependencies` | `open val` | `override val` |
| `userCollFun` | `abstract val` | `abstract override val` |
| `insertOne(item, apiFilter, call)` | `suspend fun` | `override suspend fun` |
| `findChildrenNot(item)` | `suspend fun` | `override suspend fun` |
| `getCrudPermission(call, crudTask)` | `suspend fun` | `override suspend fun` |
| All `onQuery*` / `onBefore*` / `onAfter*` hooks | `open suspend fun` | `override suspend fun` |
| `asApiItem(apiItem)` | `open suspend fun` | `override suspend fun` |
| `onValidate(apiItem, item)` | `open suspend fun` | `override suspend fun` |

**Migration:** The compiler will flag any conflicts. In most cases, just add `override` to your
existing declarations. If you had custom signatures that clash, rename them or adjust to match
the `IRepository` contract.

### 3. `changeLogCollFun` return type changed

**Before (1.x):**
```kotlin
open val changeLogCollFun: (() -> IChangeLogColl<*, *, *, *>?) = { null }
```

**After (2.0):**
```kotlin
// In IRepository (the interface contract):
val changeLogCollFun: () -> IChangeLogRepository?

// In Coll (unchanged concrete type - still works because IChangeLogColl implements IChangeLogRepository):
override val changeLogCollFun: (() -> IChangeLogColl<*, *, *, *>?) = { null }
```

**Impact:** If your code explicitly typed `changeLogCollFun` as `() -> IChangeLogColl<...>?`,
it still compiles because `IChangeLogColl` now implements `IChangeLogRepository`. No changes needed
in most cases. If you reference the return type in a variable, you may want to use the interface type
`IChangeLogRepository` for future flexibility.

### 4. `userCollFun` return type changed

**Before (1.x):**
```kotlin
abstract val userCollFun: () -> IUserColl<*, *, UID, *>?
```

**After (2.0):**
```kotlin
// In IRepository (the interface contract):
val userCollFun: () -> IUserRepository<*, UID>?

// In Coll (unchanged concrete type - still works):
abstract override val userCollFun: () -> IUserColl<*, *, UID, *>?
```

**Impact:** Same as `changeLogCollFun` — `IUserColl` now implements `IUserRepository`, so existing
concrete types are valid. If you typed variables explicitly as `IUserColl`, consider using
`IUserRepository` for engine-agnostic code.

### 5. `IUserColl` now implements `IUserRepository`

`IUserColl` added the `IUserRepository<U, UID>` interface:
```kotlin
abstract class IUserColl<...>(...) : Coll<...>(...), IUserRepository<U, UID> {
```

New method added:
```kotlin
override suspend fun findUserById(id: UID?): U? = findById(id)
```

**Impact:** If you have a class extending `IUserColl`, it automatically inherits the
`IUserRepository` implementation. No changes needed.

### 6. `IChangeLogColl` now implements `IChangeLogRepository`

`IChangeLogColl` added the `IChangeLogRepository` interface:
```kotlin
abstract class IChangeLogColl<...>(...) : Coll<...>(...), IChangeLogRepository {
```

The existing `buildChangeLog` method now has the `override` modifier.

**Impact:** No changes needed in subclasses.

---

## New Features

### SQL Database Support via `SqlRepository`

FSLib 2.0 introduces `SqlRepository`, a full SQL implementation of `IRepository` using
[Exposed](https://github.com/JetBrains/Exposed) for database access and Kotlinx Serialization
for object-to-row mapping.

#### Creating a SQL Repository

```kotlin
class CustomerSqlRepo(sqlDb: SqlDatabase) : SqlRepository<
    CustomerCommon, Customer, Int, CustomerFilter, Int
>(
    commonContainer = CustomerCommon,
    sqlDatabase = sqlDb,
    tableName = "customers",  // optional, defaults to class name in lowerCamelCase
) {
    override val userCollFun: () -> IUserRepository<*, Int>? = { null }
}
```

#### Annotations

Use `@SqlField` and `@SqlIgnoreField` on your `BaseDoc` properties to control SQL mapping:

```kotlin
@Serializable
data class Customer(
    @SqlField(name = "customer_id")  // Maps _id to "customer_id" column
    override val _id: Int,

    val name: String,

    @SqlIgnoreField  // Excluded from INSERT/UPDATE statements
    val transientField: String? = null,
)
```

#### Overriding Filter Logic

Override `buildWhereFromApiFilter` to convert your domain-specific `IApiFilter` into SQL
WHERE clauses (analogous to `matchStage` in MongoDB `Coll`):

```kotlin
override fun buildWhereFromApiFilter(
    apiFilter: CustomerFilter,
    clauses: MutableList<String>,
    args: MutableList<Pair<IColumnType<*>, Any?>>,
) {
    apiFilter.status?.let {
        clauses += "\"status\" = ?"
        args += VarCharColumnType() to it
    }
    apiFilter.masterItemId?.let {
        clauses += "\"regionId\" = ?"
        args += IntegerColumnType() to it
    }
}
```

### Backend-Agnostic Repository Interface

You can now write service code that works with any database engine:

```kotlin
class CustomerService(private val repo: IRepository<CustomerCommon, Customer, Int, CustomerFilter, Int>) {
    suspend fun getCustomer(id: Int): Customer? = repo.findById(id)
    suspend fun saveCustomer(customer: Customer): ItemState<Customer> = repo.insertOne(customer)
    suspend fun listCustomers(filter: CustomerFilter): List<Customer> = repo.findList(filter)
}

// Wire with MongoDB:
val service = CustomerService(CustomerColl())

// Wire with SQL:
val service = CustomerService(CustomerSqlRepo(sqlDatabase))
```

### Cross-Engine Dependencies

The new `repositoryFun` parameter on `IRepository.Dependency` enables dependency checks across
different database engines. For example, a MongoDB collection can verify that no SQL rows
reference an item before deleting it:

```kotlin
// In a MongoDB Coll subclass:
override val dependencies = {
    listOf(
        IRepository.Dependency(
            common = OrderCommon,
            property = Order::customerId,
            repositoryFun = { OrderSqlRepo(sqlDatabase) },  // SQL repo
        )
    )
}
```

When `repositoryFun` is provided, `findChildrenNot` calls `existsByField` on that repository
(which queries the correct engine). When `repositoryFun` is `null` (the default), the current
engine's native logic is used — preserving full backward compatibility.

### `existsByField` Method

Both `Coll` and `SqlRepository` implement the new `existsByField` method from `IRepository`:

```kotlin
suspend fun existsByField(property: KProperty1<out BaseDoc<*>, *>, value: Any?): Boolean
```

This is the engine-agnostic primitive for checking record existence by field value. It can also
be used directly in application code for custom existence checks.

---

## SQL-Specific Features

### Identifier Quoting

All SQL identifiers (table names, column names) are automatically quoted with double quotes
to prevent SQL injection and support reserved-word column names.

### Type-Aware Filters

`SqlRepository` resolves the Kotlin property type of each filtered field to bind query parameters
with the correct JDBC type (Int, Long, Double, Boolean, LocalDateTime, etc.) instead of always
using VARCHAR. This prevents type mismatch errors on strongly-typed databases like PostgreSQL.

### RBAC (Role-Based Access Control)

`SqlRepository.getCrudPermission` delegates to the shared `Coll.roleInUserColl` RBAC
infrastructure when configured. This means SQL repositories inherit the same permission system
as MongoDB collections without additional setup. If no `roleInUserColl` is configured, all
operations are permitted by default.

---

## Quick Migration Checklist

- [ ] Update `gradle/libs.versions.toml`: `fsLib = "2.0.0"`
- [ ] Replace `Coll.Dependency` with `IRepository.Dependency` (update imports)
- [ ] Add `override` modifier to any `Coll` subclass members that now implement `IRepository`
- [ ] If you explicitly typed `changeLogCollFun` or `userCollFun` with MongoDB-specific types,
      consider switching to `IChangeLogRepository` / `IUserRepository` for engine-agnostic code
- [ ] Build the project — the Kotlin compiler will flag any remaining type mismatches
- [ ] (Optional) Add `repositoryFun` to cross-engine `Dependency` declarations
- [ ] (Optional) Migrate entities to `SqlRepository` for SQL-backed tables
