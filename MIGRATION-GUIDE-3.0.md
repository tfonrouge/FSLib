# FSLib 3.0 Migration Guide

This guide covers all breaking changes, new features, and migration steps when upgrading from FSLib 2.0 to 3.0.

---

## Overview

FSLib 3.0 is a **structural reorganization** of the library. Modules have been renamed for clarity,
database engines have been extracted into standalone modules, and the sample applications have been
reorganized into a unified `samples/` directory.

### What changed at a glance

| Aspect | 2.0 | 3.0 |
|--------|-----|-----|
| Foundation module | `:base` | `:core` |
| Framework module | `:fullStack` | `:fullstack` |
| Utilities module | `:utils` | `:media` |
| MongoDB engine | Bundled in `:fullStack` | Standalone `:mongodb` |
| SQL engine | Bundled in `:fullStack` | Standalone `:sql` |
| Sample apps | Under `test1/` | Under `samples/fullstack/` and `samples/ssr/` |
| RBAC coupling | `SqlRepository` imports MongoDB types directly | Decoupled via `IRolePermissionProvider` |

### New dependency graph

```
:fullstack → :core
:mongodb   → :fullstack, :core
:sql       → :fullstack, :core
:media     → :fullstack, :core, :mongodb
:ssr       → :fullstack, :core, :mongodb
```

Applications depend on `:fullstack` plus whichever engine modules they need (`:mongodb`, `:sql`,
or both).

---

## Breaking Changes

### 1. Module renames

All three original modules have been renamed:

| 2.0 name | 3.0 name | Maven artifact |
|----------|----------|----------------|
| `:base` | `:core` | `com.fonrouge.fsLib:core` |
| `:fullStack` | `:fullstack` | `com.fonrouge.fsLib:fullstack` |
| `:utils` | `:media` | `com.fonrouge.fsLib:media` |

**Before (2.0) `build.gradle.kts`:**
```kotlin
dependencies {
    implementation("com.fonrouge.fsLib:base:2.0.0")
    implementation("com.fonrouge.fsLib:fullStack:2.0.0")
    implementation("com.fonrouge.fsLib:utils:2.0.0")
}
```

**After (3.0) `build.gradle.kts`:**
```kotlin
dependencies {
    implementation("com.fonrouge.fsLib:core:3.0.0")
    implementation("com.fonrouge.fsLib:fullstack:3.0.0")
    implementation("com.fonrouge.fsLib:media:3.0.0")
}
```

**Migration:** Update all dependency declarations and `include()` statements in `settings.gradle.kts`.
Java/Kotlin package names (`com.fonrouge.base`, `com.fonrouge.fullStack`) are **unchanged** —
no source code import changes are needed for the rename itself.

### 2. MongoDB engine extracted to `:mongodb`

All MongoDB-related server code has been moved from `:fullstack` to the new `:mongodb` module.
This includes:

- `Coll.kt` and all classes in `com.fonrouge.fullStack.mongoDb.*`
- Aggregation pipeline helpers (`aggregation/dateDiff.kt`, etc.)
- `FieldPath.kt`
- `OnlyInputTypes.kt` annotation

**Before (2.0) `build.gradle.kts`:**
```kotlin
// MongoDB came transitively through :fullStack
dependencies {
    implementation("com.fonrouge.fsLib:fullStack:2.0.0")
}
```

**After (3.0) `build.gradle.kts`:**
```kotlin
dependencies {
    implementation("com.fonrouge.fsLib:fullstack:3.0.0")
    implementation("com.fonrouge.fsLib:mongodb:3.0.0")  // NEW — required if using Coll
}
```

**Migration:** Add an explicit dependency on `:mongodb` in any module that uses `Coll`,
MongoDB aggregations, or any type from `com.fonrouge.fullStack.mongoDb`.

### 3. SQL engine extracted to `:sql`

`SqlRepository` and `SqlDatabase` have been moved from `:fullstack` to the new `:sql` module.

**Before (2.0) `build.gradle.kts`:**
```kotlin
// SqlRepository came transitively through :fullStack
dependencies {
    implementation("com.fonrouge.fsLib:fullStack:2.0.0")
}
```

**After (3.0) `build.gradle.kts`:**
```kotlin
dependencies {
    implementation("com.fonrouge.fsLib:fullstack:3.0.0")
    implementation("com.fonrouge.fsLib:sql:3.0.0")  // NEW — required if using SqlRepository
}
```

**Migration:** Add an explicit dependency on `:sql` in any module that uses `SqlRepository` or
`SqlDatabase`.

### 4. JVM-only modules require platform attributes

The `:mongodb` and `:sql` modules are JVM-only (`kotlin("jvm")` plugin). When depending on the
KMP `:fullstack` or `:core` modules, they use Gradle platform attributes. If your JVM-only module
depends on `:fullstack` or `:core`, you may need to add the same attribute:

```kotlin
dependencies {
    implementation(project(":fullstack")) {
        attributes {
            attribute(
                org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.attribute,
                org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.jvm
            )
        }
    }
}
```

This is only needed for pure JVM modules (`kotlin("jvm")` plugin) that depend on KMP modules.
KMP modules using `kotlin("multiplatform")` resolve source sets automatically.

### 5. RBAC decoupled from MongoDB via `PermissionRegistry`

In 2.0, `SqlRepository.getCrudPermission()` directly imported `Coll.roleInUserColl` and used
KMongo BSON operators. In 3.0, this coupling is removed through a new abstraction layer.

**Before (2.0) — SqlRepository internally:**
```kotlin
import com.fonrouge.fullStack.mongoDb.Coll
import org.litote.kmongo.eq

override suspend fun getCrudPermission(call: ApplicationCall, crudTask: CrudTask): SimpleState {
    val roleInUserColl = Coll.roleInUserColl ?: return SimpleState(isOk = true)
    // ... direct MongoDB query with BSON operators
}
```

**After (3.0) — SqlRepository internally:**
```kotlin
import com.fonrouge.fullStack.repository.PermissionRegistry

override suspend fun getCrudPermission(call: ApplicationCall, crudTask: CrudTask): SimpleState {
    val provider = PermissionRegistry.rolePermissionProvider ?: return SimpleState(isOk = true)
    return provider.getCrudPermission(commonContainer, call, crudTask)
}
```

**Impact on consumers:** If you subclass `SqlRepository` and override `getCrudPermission`, you
should use `PermissionRegistry` instead of directly referencing `Coll`. The MongoDB module
automatically registers its `MongoRolePermissionProvider` when a `Coll` implementing
`IRoleInUserColl` is initialized — no manual setup required.

### 6. `OnlyInputTypes` annotation moved

The `OnlyInputTypes` annotation moved from `kotlin.internal` package (which required the
`-Xallow-kotlin-package` compiler flag) to `com.fonrouge.fullStack.mongoDb`:

**Before (2.0):**
```kotlin
package kotlin.internal

@Target(AnnotationTarget.TYPE_PARAMETER)
@Retention(AnnotationRetention.BINARY)
internal annotation class OnlyInputTypes
```

**After (3.0):**
```kotlin
package com.fonrouge.fullStack.mongoDb

@Target(AnnotationTarget.TYPE_PARAMETER)
@Retention(AnnotationRetention.BINARY)
internal annotation class OnlyInputTypes
```

**Impact:** This annotation is `internal` — consumer code cannot reference it directly. If you
somehow imported `kotlin.internal.OnlyInputTypes`, update the import. In practice, this change
is transparent.

### 7. Transitive dependencies no longer available

Since `:fullstack` no longer bundles MongoDB or SQL dependencies, some transitive dependencies
that were previously available "for free" must now be added explicitly if your code uses them.

| Dependency | Previously from | Now requires |
|------------|----------------|--------------|
| `kmongo-coroutine-serialization` | `:fullStack` | `:mongodb` or explicit dep |
| `kmongo-id-serialization` | `:fullStack` | `:mongodb` or explicit dep |
| `exposed-core` / `exposed-dao` / `exposed-jdbc` | `:fullStack` | `:sql` or explicit dep |
| `kotlinx-datetime-jvm` | `:fullStack` | `:mongodb`, `:sql`, or explicit dep |
| `jtds` / `mssql-jdbc` (JDBC drivers) | `:fullStack` | `:sql` or explicit dep |

**Migration:** If compilation fails with unresolved references to KMongo, Exposed, or
kotlinx-datetime types, add the appropriate engine module or explicit library dependency.

### 8. Samples directory restructured

Sample applications moved from `test1/` to a categorized `samples/` directory:

| 2.0 path | 3.0 path |
|----------|----------|
| `test1/` | `samples/fullstack/rpc-demo/` |
| — | `samples/fullstack/greeting/` (new) |
| — | `samples/fullstack/contacts/` (new) |
| — | `samples/ssr/basic/` (new) |
| — | `samples/ssr/catalog/` (new) |
| — | `samples/ssr/advanced/` (new) |

**Impact:** If you referenced `test1` in your build scripts or CI configuration, update the paths.

---

## New Features

### Database Engine Modules

The `:mongodb` and `:sql` modules are now independent, optional dependencies. This means:

- **Smaller deployments**: Applications using only MongoDB do not pull in Exposed/JDBC. Applications
  using only SQL do not pull in KMongo.
- **Clear separation**: Each engine module contains only its own implementation — no cross-engine
  imports within the engine code.
- **Mix and match**: Applications can depend on both `:mongodb` and `:sql` for hybrid architectures.

### `IRolePermissionProvider` Interface

A new backend-agnostic interface for CRUD permission checking, located in
`com.fonrouge.fullStack.repository`:

```kotlin
interface IRolePermissionProvider {
    suspend fun getCrudPermission(
        commonContainer: ICommonContainer<*, *, *>,
        call: ApplicationCall,
        crudTask: CrudTask,
    ): SimpleState
}
```

The MongoDB module provides `MongoRolePermissionProvider` (registered automatically). Custom
implementations can be registered via `PermissionRegistry.rolePermissionProvider` for
non-MongoDB permission backends.

### `PermissionRegistry` Singleton

Global registry for the active permission provider:

```kotlin
object PermissionRegistry {
    var rolePermissionProvider: IRolePermissionProvider? = null
}
```

When `null` (the default), all CRUD operations are implicitly permitted. The MongoDB module
sets this automatically when a `Coll` implementing `IRoleInUserColl` is instantiated.

### SSR Module

The `:ssr` module provides server-side rendering support using Ktor HTML builder, with sample
applications under `samples/ssr/`.

---

## Dependency Configuration Examples

### MongoDB-only application

```kotlin
// settings.gradle.kts
include(":app")

// app/build.gradle.kts
dependencies {
    implementation("com.fonrouge.fsLib:core:3.0.0")
    implementation("com.fonrouge.fsLib:fullstack:3.0.0")
    implementation("com.fonrouge.fsLib:mongodb:3.0.0")
}
```

### SQL-only application

```kotlin
// app/build.gradle.kts
dependencies {
    implementation("com.fonrouge.fsLib:core:3.0.0")
    implementation("com.fonrouge.fsLib:fullstack:3.0.0")
    implementation("com.fonrouge.fsLib:sql:3.0.0")
}
```

### Hybrid (MongoDB + SQL) application

```kotlin
// app/build.gradle.kts
dependencies {
    implementation("com.fonrouge.fsLib:core:3.0.0")
    implementation("com.fonrouge.fsLib:fullstack:3.0.0")
    implementation("com.fonrouge.fsLib:mongodb:3.0.0")
    implementation("com.fonrouge.fsLib:sql:3.0.0")
}
```

### With media (file attachments) support

```kotlin
// app/build.gradle.kts
dependencies {
    implementation("com.fonrouge.fsLib:core:3.0.0")
    implementation("com.fonrouge.fsLib:fullstack:3.0.0")
    implementation("com.fonrouge.fsLib:mongodb:3.0.0")
    implementation("com.fonrouge.fsLib:media:3.0.0")  // replaces :utils
}
```

---

## Quick Migration Checklist

- [ ] Update `gradle/libs.versions.toml`: `fsLib = "3.0.0"`
- [ ] Rename module references: `:base` → `:core`, `:fullStack` → `:fullstack`, `:utils` → `:media`
- [ ] Update `settings.gradle.kts` `include()` statements
- [ ] Add `:mongodb` dependency if your project uses `Coll` or any `com.fonrouge.fullStack.mongoDb.*` types
- [ ] Add `:sql` dependency if your project uses `SqlRepository` or `SqlDatabase`
- [ ] Add platform attributes for JVM-only modules depending on KMP modules (`:fullstack`, `:core`)
- [ ] Check for missing transitive dependencies (`kotlinx-datetime-jvm`, Exposed, KMongo)
- [ ] If overriding `getCrudPermission` in `SqlRepository` subclasses, switch to `PermissionRegistry`
- [ ] Update any CI/CD or script references from `test1/` to `samples/`
- [ ] Build the project — the Kotlin compiler will flag any remaining unresolved references
