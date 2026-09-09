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
- **Extractable:** Pipeline building (~230 lines) into `AggregationPipelineBuilder`, ~~item copying (~60 lines) into a utility~~ (done — extracted to `ConstructorCopier`), error formatting (~20 lines).
- **Not easily extractable:** Lifecycle hooks (must remain `open` for subclass overrides), CRUD orchestration (tightly coupled to hooks + permissions + changelog), IRepository bridge methods (interface contract).
- **Verdict:** Extracting the pipeline builder is the highest-value refactor (~230 lines, cohesive responsibility, reusable). Beyond that, returns diminish because remaining methods are tightly coupled to the lifecycle orchestration.

---

## Hallazgos desde mppArel — recorrido E2E del taller, 2026-09-08 (tareas, sin abrir todavía)

Cinco huecos de fsLib/KVision medidos en mppArel durante el recorrido A→E de `EjecucionTallerMoldes` (mppArel LEDGER L-104/L-106, G-022). Decisión del owner (opción a): quedan aquí como tareas; ninguna se abre ahora. Cada una trae el síntoma, dónde vive y qué se sugiere, para que el que la tome no vuelva a medir.

**Estado 2026-09-08 — abiertas por instrucción del owner en el blueprint [`blueprints/view-consumer-gaps/`](blueprints/view-consumer-gaps/BRIEF.md):** T-3 (`FsTomSelectRemote*` con guarda en vuelo, condición de retiro al estilo FsTabPanel), T-1 (diálogo de cancelar vía `gettext`) y T-4 (overload con `viewLabelProvider` + i18n de helpButtons) **construidos y con pruebas verdes** — pendiente release y la medición 1-RPC en mppArel (PLAN P1.9, rollup OC-01); la migración del consumidor está en su `CONTRACT.md`. T-2 y T-5 quedan como **decisiones de diseño registradas** (LEDGER L-006/L-007) pendientes de decisión del owner — no reabrir análisis: las opciones ya están ahí.

| # | Tarea | Síntoma medido | Dónde | Sugerencia |
|---|-------|----------------|-------|------------|
| T-1 | **Confirm de cancelar en español** | Al cancelar una ficha con cambios: «Please Confirm / Cancel and forget current changes? / No / Yes». | `fullstack/.../view/ViewItem.kt` `backCloseAction` (el `Confirm` de KVision que reemplazó al `confirm()` nativo). | Pasar caption/texto/botones por `I18n` (mppArel ya instala `messagesEs`) o por parámetros del `ViewItem`. |
| T-2 | **El layout persistido congela título y orden de columnas** | `ViewList.tabulatorInfoPersistenceConfig = true` guarda `field/title/width/visible` y el orden. Renombrar o mover una columna en código NO llega a ningún navegador que ya abrió la lista; mppArel lo tapa con un saneo por firma (`layoutPersistido.kt`, `LAYOUTS_OBSOLETOS`). | `fullstack/.../view/ViewList.kt` (companion `tabulatorInfoPersistenceConfig`). | Persistir sólo `width`/`visible` (`persistence.columns = ["width","visible"]`) y decidir qué hacer con el ORDEN (Tabulator lo persiste igual): o un `layoutVersion` por vista que invalide el guardado, o que las columnas nuevas/renombradas se reconcilien por `field` con la definición de código como fuente del título. |
| T-3 | **`TomSelectRemoteInput.refreshState` sin guarda de carga en vuelo (KVision 9.5.0)** | Al abrir cualquier ficha, cada `tomSelectRemote` dispara 7-9 RPC idénticas simultáneas y el `<select>` nativo acumula opciones duplicadas (OT: Usuario ×8, Depto ×7; actividad: Depto ×8, Centro ×7; ficha de componente ×9). Causa: `refreshState()` relanza `loadResults` cada vez que se le llama mientras `options[value]` aún no llegó, y el poblado del form lo llama N veces. | KVision `kvision-tom-select-remote` `TomSelectRemoteInput.kt:117-127`; el disparador (N `refreshState` durante `setData`) es de fsLib/KVision. | Subclase en fsLib con bandera de carga en vuelo (una petición por valor pendiente) o parche aguas arriba en KVision; medir con `performance.getEntriesByType('resource')` — objetivo 1 RPC por selector al abrir. |
| T-4 | **La cabecera de la ayuda se captura al crear el FAB** | `helpButtons(viewClassName, viewLabel, …)` fija `caption = "Ayuda — $viewLabel"` una vez; en una ficha en lectura el item aún no cargó y la cabecera queda «Componente del Herramental/Equipo:» (antes «…: null», corregido en mppArel en los `labelId`). | `fullstack/.../layout/helpButtons.kt:431-438`. | Recibir `viewLabel` como proveedor (`() -> String`) y evaluarlo al abrir el offcanvas/modal, no al construir el botón. |
| T-5 | **`openViewItem(modal)` sin callback de cierre ni de escritura** (ya en mppArel G-022) | Una vista que abre una ficha en modal no puede enterarse de que se guardó o cerró; mppArel lo resolvió con un bus de aplicación (`AvisosDeTaller`) emitido desde `onUpsertResult`, y antes con un listener `hidden.bs.modal` en `document`. | `fullstack/.../config/ConfigViewItem.kt` `openViewItem` / `Modal(...).disposeOnHidden()`. | `openViewItem(modal, onClosed: ((ItemState<T>?) -> Unit)? = null)` que reciba el resultado de la escritura (si la hubo) al cerrar; `onUpsertResult` ya es el punto donde fsLib sabe que la escritura fue completa. |

Origen y evidencia: mppArel `blueprints/EjecucionTallerMoldes(MODULE)/LEDGER.md` L-104 (recorrido), L-106 (board), G-022 (modal). Medido sobre fsLib 6.2.4 y KVision 9.6.0 (T-3 verificado contra las fuentes de `kvision-tom-select-remote` 9.6.0: `refreshState` idéntico, mismas líneas 117-127; el diff 9.5.0→9.6.0 es sólo la migración `CallAgent`/`Request`).
