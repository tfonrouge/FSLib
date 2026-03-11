# FSLib Samples

Runnable examples that demonstrate FSLib features. Each sample is a self-contained Gradle sub-project.

## Fullstack Samples

Built with Kotlin Multiplatform (JVM + JS), Ktor server, and KVision frontend.

| Sample | Description |
|--------|-------------|
| **greeting** | Minimal RPC app — text input calls a server function and shows the result via toast. |
| **contacts** | Tabulator data grid loaded via RPC with local pagination, plus create/delete operations. |
| **rpc-demo** | Advanced KVision demo with dropdown menus, i18n, cell editing, and multiple webpack configs. |
| **showcase** | Full FSLib view system: ViewList, ViewItem, ConfigView, ViewRegistry, formRow/formColumn, Tabulator with server-side pagination/filtering/sorting, CRUD lifecycle, and InMemoryRepository backend. |

### Running a fullstack sample

Each sample has a `run` task that builds the JS frontend and starts the Ktor backend in a single command:

```bash
./gradlew :samples:fullstack:showcase:run
./gradlew :samples:fullstack:contacts:run
./gradlew :samples:fullstack:greeting:run
./gradlew :samples:fullstack:rpc-demo:run
```

Then open [http://localhost:8080](http://localhost:8080) in your browser.

### Development with hot reload

For frontend development with webpack hot module replacement, run two terminals:

```bash
# Terminal 1 — backend (API server on port 8080)
./gradlew :samples:fullstack:<sample>:jvmRun

# Terminal 2 — frontend dev server (port 3000, proxies API to 8080)
./gradlew :samples:fullstack:<sample>:jsRun
```

Then open [http://localhost:3000](http://localhost:3000) instead.

## SSR Samples

Server-Side Rendering samples built with Ktor HTML builder (JVM-only, no JS frontend).

| Sample | Description |
|--------|-------------|
| **basic** | Minimal Todo CRUD app with in-memory storage. |
| **catalog** | Product + Customer catalog with master-detail views. |
| **advanced** | Project/Task tracker with nested views, validation, and advanced SSR features. |

```bash
./gradlew :samples:ssr:basic:run
./gradlew :samples:ssr:catalog:run
./gradlew :samples:ssr:advanced:run
```

Then open [http://localhost:8080](http://localhost:8080) in your browser.

## Prerequisites

- JDK 21+
- No database required — all samples use in-memory storage.
