# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

FSLib is a Kotlin Multiplatform library (`com.fonrouge.fsLib`) for building full-stack web applications with MongoDB and/or SQL backends and KVision frontend. It provides CRUD scaffolding via a backend-agnostic `IRepository` interface, view management, Tabulator integration, and shared data models across JVM/JS targets.

## Build Commands

```bash
./gradlew build                    # Build all modules
./gradlew :base:build              # Build only the base module
./gradlew :fullStack:build         # Build only the fullStack module
./gradlew :utils:build             # Build only the utils module
./gradlew :test1:build             # Build the test application
./gradlew :test1:jvmRun            # Run the test1 Ktor server (main class: io.ktor.server.netty.EngineMain)
./gradlew :test1:jsRun             # Run the test1 JS dev server
./gradlew :test1:jvmTest           # Run JVM tests in test1
./gradlew :test1:jsTest            # Run JS tests in test1 (uses Chrome headless via Karma)
./gradlew publishToMavenLocal      # Publish library modules to local Maven
```

## Architecture

### Module Dependency Graph

```
test1 → fullStack → base
utils → fullStack → base
```

- **`:base`** — Platform-independent foundation (commonMain/jvmMain/jsMain). Contains `BaseDoc<ID>` (the document interface all models implement), common interfaces (`ICommon`, `ICommonContainer`), date/math utilities, custom serializers (BSON types, dates), SQL database support (`SqlDatabase`), SQL annotations (`@SqlField`, `@SqlIgnoreField`, `@SqlOneToOne`), coroutine helpers, user session/role models, and API interfaces.

- **`:fullStack`** — Core library module (commonMain/jvmMain/jsMain). Uses Kilua RPC plugin for frontend-backend communication.
  - **jvmMain**: `IRepository` — backend-agnostic interface for CRUD, list queries, lifecycle hooks, permissions, and dependencies. `Coll<T: BaseDoc>` — MongoDB implementation providing aggregation pipelines, lookups, filtering, change logging, and role-based access (built on KMongo coroutine driver). `SqlRepository` — SQL implementation using Exposed for relational database access.
  - **jsMain**: View system — `View`, `ViewItem`, `ViewList`, `ViewFormPanel`, `ViewDataContainer` for rendering CRUD views. `ConfigView`/`ConfigViewItem`/`ConfigViewList`/`ConfigViewContainer` for declarative view configuration. Tabulator wrappers (`TabulatorViewList`, `fsTabulator`) for data grids. Layout helpers (`formRow`, `formColumn`, `toolBarList`, etc.). `ViewRegistry` — centralized registry for view configurations and RPC service managers.
  - **commonMain**: Shared RPC service interfaces, API definitions.

- **`:utils`** — Extension module adding DataMedia (file/attachment handling) and ChangeLog views on top of fullStack.

- **`:test1`** — Sample/test application using the KVision plugin. Runnable full-stack app with Ktor/Netty backend.

### Key Patterns

- **Kotlin Multiplatform with `expect`/`actual`**: Source sets are `commonMain`, `jvmMain`, `jsMain`. Shared interfaces in commonMain; platform implementations in jvmMain (MongoDB/Ktor) and jsMain (KVision/browser).
- **KMongo + coroutines**: Server-side MongoDB access via `CoroutineCollection` from KMongo. The `Coll` class wraps this with CRUD operations, aggregation pipelines, and BSON manipulation.
- **KVision**: Frontend UI framework. Views extend KVision components. Tabulator is used for data grids with remote data loading.
- **Kilua RPC**: Used in `:fullStack` for type-safe RPC service definitions shared between client and server.
- **Kotlinx Serialization**: All models use `@Serializable`. Custom serializers exist for BSON ObjectId, LocalDate, LocalDateTime, OffsetDateTime, and numeric types.
- **Repository abstraction**: `IRepository` defines the backend-agnostic contract for CRUD, list queries, lifecycle hooks, permissions, and dependency checking. `Coll` (MongoDB) and `SqlRepository` (SQL/Exposed) both implement it. Related interfaces: `IUserRepository`, `IChangeLogRepository`.
- **State management**: `State`, `ItemState`, `ListState`, `SimpleState` in base module for managing UI/data state.

### Technology Stack

- Kotlin 2.3.x, Gradle with version catalogs (`gradle/libs.versions.toml`)
- Backend: Ktor (Netty), MongoDB (KMongo), Exposed (SQL), JWT auth
- Frontend: KVision 9.x, Bootstrap, Tabulator, FontAwesome
- JVM toolchain: 21

### Coding Conventions

- Always add KDoc comments to any created or updated class, function, struct, interface, or any other code construct.

### SQL Annotations

Located in `base/src/commonMain/kotlin/com/fonrouge/base/annotations/`:

- `@SqlField(name, compound)` — Maps a property to a specific SQL column name or marks it as compound.
- `@SqlIgnoreField` — Excludes the property from SQL INSERT/UPDATE statements.
- `@SqlOneToOne` — Marks a one-to-one relationship for SQL mapping.

### Help Documentation

The file `HELP-DOCS-GUIDE.md` contains the guide for the help documentation system. It must be copied to any project that intends to use the help docs functionality provided by this library.

### Migration Guide

The file `MIGRATION-GUIDE-2.0.md` documents all breaking changes, new features, and migration steps from FSLib 1.x to 2.0 (dual database engine support).

### Language

Code comments, KDoc, and user-facing strings should be written in **English**. The project uses KVision's i18n module for internationalization, allowing downstream applications to provide translations for any target language.
