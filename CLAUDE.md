# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

FSLib is a Kotlin Multiplatform library (`com.fonrouge.fsLib`) for building full-stack web applications with MongoDB and/or SQL backends and KVision frontend. It provides CRUD scaffolding via a backend-agnostic `IRepository` interface, view management, Tabulator integration, and shared data models across JVM/JS targets.

## Build Commands

```bash
./gradlew build                    # Build all modules
./gradlew :core:build              # Build the core module
./gradlew :fullstack:build         # Build the fullstack module
./gradlew :mongodb:build           # Build the MongoDB engine module
./gradlew :sql:build               # Build the SQL engine module
./gradlew :media:build             # Build the media module
./gradlew :ssr:build               # Build the SSR module
./gradlew :ssr:test                # Run SSR tests
./gradlew publishToMavenLocal      # Publish library modules to local Maven
```

### Sample Applications

```bash
./gradlew :samples:ssr:basic:run           # Run SSR basic sample
./gradlew :samples:ssr:catalog:run         # Run SSR catalog sample
./gradlew :samples:ssr:advanced:run        # Run SSR advanced sample
./gradlew :samples:fullstack:rpc-demo:jvmRun   # Run fullstack RPC demo (Ktor server)
./gradlew :samples:fullstack:rpc-demo:jsRun    # Run fullstack RPC demo (JS dev server)
./gradlew :samples:fullstack:greeting:jvmRun   # Run greeting sample
./gradlew :samples:fullstack:contacts:jvmRun   # Run contacts sample
```

## Architecture

### Module Dependency Graph

```
samples:fullstack:* → fullstack → core
                      mongodb ↗
samples:ssr:*       → ssr → fullstack → core
                         → mongodb ↗
media               → mongodb → fullstack → core
sql                 → fullstack → core
```

- **`:core`** — Platform-independent foundation (commonMain/jvmMain/jsMain). Contains `BaseDoc<ID>` (the document interface all models implement), common interfaces (`ICommon`, `ICommonContainer`), date/math utilities, custom BSON-aware serializers (ObjectId, dates, numeric types), SQL annotations (`@SqlField`, `@SqlIgnoreField`, `@SqlOneToOne`), coroutine helpers, user session/role models, state management, and API interfaces. Uses `kmongo-coroutine-serialization` for BSON serializer actuals, `ktor-server-core` for `ApplicationCall` typealias, and `kvision-common-remote` for shared date types. Source packages: `com.fonrouge.base.*`.

- **`:fullstack`** — Core framework module (commonMain/jvmMain/jsMain). Uses Kilua RPC plugin for frontend-backend communication. Database-engine-agnostic.
  - **jvmMain**: `IRepository` — backend-agnostic interface for CRUD, list queries, lifecycle hooks, permissions, and dependencies. `IRolePermissionProvider` / `PermissionRegistry` — abstraction for role-based permission checks. `IUserRepository`, `IChangeLogRepository` — backend-agnostic interfaces. `HelpDocsService` — help documentation service. Full Ktor client/server stack.
  - **jsMain**: View system — `View`, `ViewItem`, `ViewList`, `ViewFormPanel`, `ViewDataContainer` for rendering CRUD views. `ConfigView`/`ConfigViewItem`/`ConfigViewList`/`ConfigViewContainer` for declarative view configuration. Tabulator wrappers (`TabulatorViewList`, `fsTabulator`) for data grids. Layout helpers (`formRow`, `formColumn`, `toolBarList`, etc.). `ViewRegistry` — centralized registry for view configurations and RPC service managers. Full KVision UI stack (Bootstrap, FontAwesome, Tabulator, etc.). Also includes UI utility helpers (`toast`, `buttonMenu`, form control helpers, etc.).
  - **commonMain**: Shared RPC service interfaces, API definitions. Source packages: `com.fonrouge.fullStack.*`.

- **`:mongodb`** — MongoDB database engine (JVM-only). `Coll<T: BaseDoc>` — MongoDB implementation of `IRepository` providing aggregation pipelines, lookups, filtering, change logging, and role-based access (built on KMongo coroutine driver). `MongoDb` — database connection management. `FieldPath` — nested property path builder. Registers `MongoRolePermissionProvider` with `PermissionRegistry` for cross-engine permission checks.

- **`:sql`** — SQL database engine (JVM-only). `SqlRepository` — SQL implementation of `IRepository` using Exposed for relational database access. `SqlDatabase` — SQL connection management with MSSQL/jTDS JDBC drivers. Uses `PermissionRegistry` for role-based access control.

- **`:ssr`** — Server-Side Rendering module (JVM-only). Provides `PageDef`, `FormContext`, `ColumnDef`, layout builders, and Ktor HTML integration for building server-rendered CRUD pages without a JS frontend.

- **`:media`** — Extension module (commonMain/jvmMain/jsMain) adding DataMedia (file/attachment handling) and ChangeLog views on top of fullstack and mongodb.

- **`samples/`** — Top-level directory containing sample applications:
  - `samples/ssr/basic/` — Minimal Todo CRUD app
  - `samples/ssr/catalog/` — Product + Customer catalog
  - `samples/ssr/advanced/` — Project/Task tracker with advanced features
  - `samples/fullstack/rpc-demo/` — KVision + Kilua RPC demo
  - `samples/fullstack/greeting/` — Minimal RPC greeting app
  - `samples/fullstack/contacts/` — Tabulator grid with contacts

### Key Patterns

- **Kotlin Multiplatform with `expect`/`actual`**: Source sets are `commonMain`, `jvmMain`, `jsMain`. Shared interfaces in commonMain; platform implementations in jvmMain (MongoDB/Ktor) and jsMain (KVision/browser). Expect/actual pairs must reside in the same module.
- **KMongo + coroutines**: Server-side MongoDB access via `CoroutineCollection` from KMongo. The `Coll` class wraps this with CRUD operations, aggregation pipelines, and BSON manipulation.
- **KVision**: Frontend UI framework. Views extend KVision components. Tabulator is used for data grids with remote data loading.
- **Kilua RPC**: Used in `:fullstack` for type-safe RPC service definitions shared between client and server.
- **Kotlinx Serialization**: All models use `@Serializable`. Custom serializers exist for BSON ObjectId, LocalDate, LocalDateTime, OffsetDateTime, and numeric types — these live in `:core` alongside the models that reference them.
- **Repository abstraction**: `IRepository` in `:fullstack` defines the backend-agnostic contract for CRUD, list queries, lifecycle hooks, permissions, and dependency checking. `Coll` in `:mongodb` and `SqlRepository` in `:sql` both implement it. `IRolePermissionProvider` / `PermissionRegistry` decouple permission checks from any specific database engine. Related interfaces: `IUserRepository`, `IChangeLogRepository`.
- **State management**: `State`, `ItemState`, `ListState`, `SimpleState` in core module for managing UI/data state.

### Technology Stack

- Kotlin 2.3.x, Gradle with version catalogs (`gradle/libs.versions.toml`)
- Backend: Ktor (Netty), MongoDB (KMongo), Exposed (SQL), JWT auth
- Frontend: KVision 9.x, Bootstrap, Tabulator, FontAwesome
- JVM toolchain: 21

### Coding Conventions

- Always add KDoc comments to any created or updated class, function, struct, interface, or any other code construct.

### SQL Annotations

Located in `core/src/commonMain/kotlin/com/fonrouge/base/annotations/`:

- `@SqlField(name, compound)` — Maps a property to a specific SQL column name or marks it as compound.
- `@SqlIgnoreField` — Excludes the property from SQL INSERT/UPDATE statements.
- `@SqlOneToOne` — Marks a one-to-one relationship for SQL mapping.

### Help Documentation

The file `HELP-DOCS-GUIDE.md` contains the full guide for the help documentation system. It must be copied to any project that intends to use the help docs functionality provided by this library.

**Key concepts:**
- Three help types: **Tutorial** (`tutorial.html`), **Context Help** (`context.html`), and **Module Manual** (`manual.html`).
- Files live under `help-docs/{module-slug}/{ViewClassName}/` (tutorial/context) or `help-docs/{module-slug}/manual.html` (manual).
- Views declare their module via `override val helpModule: IHelpModule`. Discovery can be disabled with `override val helpEnabled: Boolean = false`.
- The UI auto-discovers help files and shows a floating "?" dropdown: view help opens in an offcanvas panel (with tabs if multiple types exist), module manual opens in a modal with iframe.
- Content tone should be **enjoyable and fun** — friendly, conversational, with light humor where appropriate.
- Content language is determined by each downstream project (language-agnostic).

### Migration Guide

The file `MIGRATION-GUIDE-2.0.md` documents all breaking changes, new features, and migration steps from FSLib 1.x to 2.0 (dual database engine support).

### Language

Code comments, KDoc, and user-facing strings should be written in **English**. The project uses KVision's i18n module for internationalization, allowing downstream applications to provide translations for any target language.
