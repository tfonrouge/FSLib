# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

FSLib is a Kotlin Multiplatform library (`com.fonrouge.fsLib`) for building full-stack web applications with MongoDB backend and KVision frontend. It provides CRUD scaffolding, view management, Tabulator integration, and shared data models across JVM/JS targets.

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

- **`:base`** — Platform-independent foundation (commonMain/jvmMain/jsMain). Contains `BaseDoc<ID>` (the document interface all models implement), common interfaces (`ICommon`, `ICommonContainer`), date/math utilities, custom serializers (BSON types, dates), SQL database support (`SqlDatabase`), coroutine helpers, user session/role models, and API interfaces.

- **`:fullStack`** — Core library module (commonMain/jvmMain/jsMain). Uses Kilua RPC plugin for frontend-backend communication.
  - **jvmMain**: `Coll<T: BaseDoc>` — the central MongoDB collection wrapper providing CRUD, aggregation pipelines, lookups, filtering, change logging, and role-based access. Built on KMongo coroutine driver.
  - **jsMain**: View system — `View`, `ViewItem`, `ViewList`, `ViewFormPanel`, `ViewDataContainer` for rendering CRUD views. `ConfigView`/`ConfigViewItem`/`ConfigViewList`/`ConfigViewContainer` for declarative view configuration. Tabulator wrappers (`TabulatorViewList`, `fsTabulator`) for data grids. Layout helpers (`formRow`, `formColumn`, `toolBarList`, etc.).
  - **commonMain**: Shared RPC service interfaces, API definitions.

- **`:utils`** — Extension module adding DataMedia (file/attachment handling) and ChangeLog views on top of fullStack.

- **`:test1`** — Sample/test application using the KVision plugin. Runnable full-stack app with Ktor/Netty backend.

### Key Patterns

- **Kotlin Multiplatform with `expect`/`actual`**: Source sets are `commonMain`, `jvmMain`, `jsMain`. Shared interfaces in commonMain; platform implementations in jvmMain (MongoDB/Ktor) and jsMain (KVision/browser).
- **KMongo + coroutines**: Server-side MongoDB access via `CoroutineCollection` from KMongo. The `Coll` class wraps this with CRUD operations, aggregation pipelines, and BSON manipulation.
- **KVision**: Frontend UI framework. Views extend KVision components. Tabulator is used for data grids with remote data loading.
- **Kilua RPC**: Used in `:fullStack` for type-safe RPC service definitions shared between client and server.
- **Kotlinx Serialization**: All models use `@Serializable`. Custom serializers exist for BSON ObjectId, LocalDate, LocalDateTime, OffsetDateTime, and numeric types.
- **State management**: `State`, `ItemState`, `ListState`, `SimpleState` in base module for managing UI/data state.

### Technology Stack

- Kotlin 2.3.x, Gradle with version catalogs (`gradle/libs.versions.toml`)
- Backend: Ktor (Netty), MongoDB (KMongo), Exposed (SQL), JWT auth
- Frontend: KVision 9.x, Bootstrap, Tabulator, FontAwesome
- JVM toolchain: 21 (in fullStack and test1)

### Coding Conventions

- Always add KDoc comments to any created or updated class, function, struct, interface, or any other code construct.

### Help Documentation

The file `HELP-DOCS-GUIDE.md` contains the guide for the help documentation system. It must be copied to any project that intends to use the help docs functionality provided by this library.

### Language

Code comments and user-facing strings are primarily in **Spanish**. The project uses KVision's i18n module for internationalization.
