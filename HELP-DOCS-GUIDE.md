# Help System — Unified Build Guide

> **Version:** 1.1.0

## Overview

The help system provides three types of documentation, all sharing a **dark theme** with emoji icons and visual components:

| Type | File | Scope | Audience | Layout |
|------|------|-------|----------|--------|
| 📦 **Module Manual** | `manual.html` | Entire module | Anyone learning the business process | Sidebar + scrollable content |
| 📖 **Tutorial** | `tutorial.html` | Single view | First-time user of this screen | Panel (no sidebar) |
| ⚡ **Context Help** | `context.html` | Single view | Experienced user needing a reminder | Panel (no sidebar) |

## Directory Structure

```
app-module/
└── help-docs/
    ├── {module-slug}/                    ← e.g. importaciones/
    │   ├── manual.html                   ← Module manual (sidebar layout)
    │   ├── ViewListEntity/
    │   │   ├── tutorial.html
    │   │   └── context.html
    │   ├── ViewItemEntity/
    │   │   ├── tutorial.html
    │   │   └── context.html
    │   └── ...
    ├── {another-module}/
    │   ├── manual.html
    │   └── ...
    └── ViewWithoutModule/                ← Views not assigned to a module
        ├── tutorial.html
        └── context.html
```

### Configuration

Consumer applications must wire up both the server and client sides.

#### Server side (JVM) — in `Main.kt` or Ktor module setup

Create a concrete subclass annotated with `@RpcService` and register it:

```kotlin
@RpcService
class MyHelpDocsService(call: ApplicationCall) : HelpDocsService(call)

// In your Kilua RPC initialization (initRpc or equivalent):
registerService<IHelpDocsService> { MyHelpDocsService(it) }

// Optional — change the help-docs root directory (default is "help-docs"):
HelpDocsService.setHelpDocsDir("help-docs")
```

The `@RpcService` annotation must be on your subclass (not on the library's `HelpDocsService`)
so that KSP generates the RPC proxy in your project's scope.

#### Client side (JS) — in app startup

Register the KSP-generated proxy with the library's registry:

```kotlin
import com.fonrouge.fullStack.services.HelpDocsServiceRegistry
import dev.kilua.rpc.getService

HelpDocsServiceRegistry.service = getService<IHelpDocsService>()
```

Without this registration, the help "?" button will not appear (no errors are thrown).

### Module Grouping (fsLib)

Views declare their module via `helpModule`:

```kotlin
// In fsLib — interface for module identification
interface IHelpModule {
    val slug: String      // directory name under help-docs/
    val displayName: String
}

// In consumer app — sealed class for type-safe modules
sealed class AppModule(
    override val slug: String,
    override val displayName: String
) : IHelpModule {
    data object Importaciones : AppModule("importaciones", "Gestión de Importaciones")
    data object Inventario : AppModule("inventario", "Inventario")
    // ...
}

// In view definition
class ViewListImportProject : ViewList<...>() {
    override val helpModule: IHelpModule = AppModule.Importaciones
}
```

---

## Shared Dark Theme

All three file types use the same dark color palette and typography.

### CSS Variables

```css
:root {
  /* Backgrounds */
  --bg: #0d1117;
  --bg-card: #111633;
  --bg-sidebar: #101538;      /* Manual only */

  /* Borders */
  --border: #22295a;
  --border-light: #2a2f5f;

  /* Text */
  --text: #e8ebff;
  --text-muted: #9da4d1;
  --text-dim: #b2b7d8;

  /* Accent colors */
  --accent-blue: #38bdf8;
  --accent-purple: #7f6bff;
  --accent-green: #1ed8a4;
  --accent-yellow: #f5b43f;
  --accent-red: #e05252;
  --accent-cyan: #00e5e5;
}
```

### Typography

- Font: `'Segoe UI', system-ui, sans-serif`
- Body text: `var(--text-dim)`, line-height 1.7
- Section titles: 1.5rem (manual) / 1.1em (tutorial/context), 700 weight
- Use **emojis as section icons** (📖, 👥, 🗺️, 📋, 🔔, 📚, ❓, 💡, ⚡, 🎯, etc.)

### Character Encoding

- Use **direct UTF-8 characters** for accented text: á, é, í, ó, ú, ñ, ¿, ü, —
- Do NOT use HTML entities (`&aacute;`, `&eacute;`, etc.)
- Use `<strong>` for key terms, `<em>` for emphasis, `<code>` for system identifiers

---

## Shared Components

These components are available in all three file types.

### 1. Cards (`.card`)

```html
<div class="card">
  <div class="card-title">📋 Title</div>
  <p>Description text.</p>
</div>
```

```css
.card {
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: 10px;
  padding: 20px;
  margin: 12px 0;
}
.card-title {
  font-weight: 700;
  font-size: 1.05rem;
  color: var(--text);
  margin-bottom: 8px;
}
```

### 2. Tip Boxes (`.tip`)

Four severity levels with emoji icons:

```html
<div class="tip tip-info">
  <span class="tip-icon">💡</span>
  <p><strong>Consejo.</strong> Description text.</p>
</div>

<div class="tip tip-warn">
  <span class="tip-icon">⚠️</span>
  <p><strong>Atención.</strong> Description text.</p>
</div>

<div class="tip tip-danger">
  <span class="tip-icon">🚨</span>
  <p><strong>Importante.</strong> Description text.</p>
</div>

<div class="tip tip-success">
  <span class="tip-icon">✅</span>
  <p><strong>Listo.</strong> Description text.</p>
</div>
```

```css
.tip {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  padding: 14px 18px;
  border-radius: 8px;
  margin: 12px 0;
  border-left: 4px solid;
}
.tip-icon { font-size: 1.3rem; flex-shrink: 0; margin-top: 2px; }
.tip p { margin: 0; color: var(--text-dim); font-size: 0.92rem; }
.tip-info { background: rgba(56,189,248,0.08); border-color: var(--accent-blue); }
.tip-warn { background: rgba(245,180,63,0.08); border-color: var(--accent-yellow); }
.tip-danger { background: rgba(224,82,82,0.08); border-color: var(--accent-red); }
.tip-success { background: rgba(30,216,164,0.08); border-color: var(--accent-green); }
```

### 3. Tables

```html
<table>
  <thead><tr><th>Columna</th><th>Descripción</th></tr></thead>
  <tbody><tr><td>Valor</td><td>Descripción</td></tr></tbody>
</table>
```

```css
table { width: 100%; border-collapse: collapse; margin: 12px 0; font-size: 0.9rem; }
th {
  background: rgba(127,107,255,0.15);
  color: var(--accent-purple);
  padding: 10px 14px;
  text-align: left;
  font-weight: 600;
  border-bottom: 2px solid var(--border);
}
td {
  padding: 8px 14px;
  border-bottom: 1px solid var(--border-light);
  color: var(--text-dim);
}
tr:hover td { background: rgba(127,107,255,0.05); }
```

### 4. Tags/Badges (`.tag`)

```html
<span class="tag tag-blue">FOB</span>
<span class="tag tag-green">Activo</span>
<span class="tag tag-red">Bloqueado</span>
<span class="tag tag-yellow">Pendiente</span>
<span class="tag tag-purple">Premium</span>
<span class="tag tag-cyan">En tránsito</span>
```

```css
.tag {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 0.78rem;
  font-weight: 600;
}
.tag-blue { background: rgba(56,189,248,0.15); color: var(--accent-blue); }
.tag-green { background: rgba(30,216,164,0.15); color: var(--accent-green); }
.tag-red { background: rgba(224,82,82,0.15); color: var(--accent-red); }
.tag-yellow { background: rgba(245,180,63,0.15); color: var(--accent-yellow); }
.tag-purple { background: rgba(127,107,255,0.15); color: var(--accent-purple); }
.tag-cyan { background: rgba(0,229,229,0.15); color: var(--accent-cyan); }
```

### 5. Glossary Grid (`.glossary-grid`)

```html
<div class="glossary-grid">
  <div class="glossary-item">
    <div class="glossary-term">📌 Término</div>
    <div class="glossary-def">Definición del término.</div>
  </div>
</div>
```

```css
.glossary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
  margin: 12px 0;
}
.glossary-item {
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: 8px;
  padding: 14px;
}
.glossary-term { font-weight: 700; color: var(--accent-blue); margin-bottom: 4px; }
.glossary-def { color: var(--text-dim); font-size: 0.88rem; }
```

### 6. FAQ Accordion (`.faq-item`)

```html
<div class="faq-item">
  <div class="faq-question" onclick="toggleFaq(this)">
    ❓ ¿Pregunta aquí? <span class="faq-chevron">▼</span>
  </div>
  <div class="faq-answer">
    Respuesta aquí.
  </div>
</div>
```

Requires JavaScript:
```javascript
function toggleFaq(el) { el.parentElement.classList.toggle('open'); }
```

```css
.faq-question {
  background: var(--bg-card);
  padding: 14px 18px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: var(--text);
}
.faq-chevron { transition: transform 0.2s; font-size: 0.8rem; color: var(--text-muted); }
.faq-item.open .faq-chevron { transform: rotate(180deg); }
.faq-answer {
  max-height: 0;
  overflow: hidden;
  transition: max-height 0.3s, padding 0.3s;
  padding: 0 18px;
  color: var(--text-dim);
  font-size: 0.92rem;
}
.faq-item.open .faq-answer { max-height: 600px; padding: 12px 18px; }
```

---

## Tutorial & Context — Panel Layout

Tutorial and context files share the same panel layout (no sidebar). They are displayed inside in-app help panels.

### Base HTML Template

```html
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>{Type} — {View Name}</title>
  <style>
    :root {
      --bg: #0d1117;
      --bg-card: #111633;
      --border: #22295a;
      --border-light: #2a2f5f;
      --text: #e8ebff;
      --text-muted: #9da4d1;
      --text-dim: #b2b7d8;
      --accent-blue: #38bdf8;
      --accent-purple: #7f6bff;
      --accent-green: #1ed8a4;
      --accent-yellow: #f5b43f;
      --accent-red: #e05252;
      --accent-cyan: #00e5e5;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body {
      font-family: 'Segoe UI', system-ui, sans-serif;
      max-width: 900px;
      margin: 0 auto;
      padding: 24px 16px;
      background: var(--bg);
      color: var(--text-dim);
      font-size: 0.92em;
      line-height: 1.7;
    }
    h1 {
      color: var(--accent-purple);
      text-align: center;
      font-size: 1.35em;
      margin-bottom: 4px;
    }
    .subtitle {
      text-align: center;
      color: var(--text-muted);
      font-size: 0.85em;
      margin-bottom: 24px;
    }
    h2 {
      color: var(--accent-blue);
      font-size: 1.1em;
      margin-top: 28px;
      margin-bottom: 10px;
      border-left: 4px solid var(--accent-purple);
      padding-left: 12px;
    }
    h3 {
      color: var(--text);
      font-size: 0.98em;
      margin-top: 16px;
      margin-bottom: 6px;
    }
    p, li { font-size: 0.9em; }
    ul { padding-left: 18px; margin: 6px 0; }
    hr { border: none; border-top: 1px solid var(--border); margin: 22px 0; }

    /* === Shared components (card, tip, table, tag, glossary, faq) === */
    /* Copy from Shared Components section above */

    /* === Tutorial: Step Cards === */
    .step-card {
      background: var(--bg-card);
      border: 1px solid var(--border-light);
      border-radius: 8px;
      padding: 14px 18px;
      margin: 10px 0;
    }
    .step-title {
      font-weight: 700;
      color: var(--accent-purple);
      margin-bottom: 6px;
      font-size: 0.95em;
    }
    .step-card p { color: var(--text-dim); }

    /* === Context: Action Cards === */
    .action-card {
      background: var(--bg-card);
      border: 1px solid var(--border-light);
      border-radius: 8px;
      padding: 12px 16px;
      margin: 10px 0;
    }
    .action-card b { color: var(--accent-blue); }
    .action-card p { color: var(--text-dim); margin: 0; }
  </style>
</head>
<body>
  <!-- Content here (no wrapper div needed) -->
</body>
</html>
```

### Tutorial-Specific Components

#### Step Cards

```html
<div class="step-card">
  <div class="step-title">📌 Paso 1 — Acceder a la vista</div>
  <p>Navegue al módulo y seleccione la opción correspondiente.</p>
</div>
```

### Context-Specific Components

#### Action Cards

```html
<div class="action-card">
  <b>➕ Crear registro</b> — Botón <b>+</b> en la barra de herramientas.
</div>

<div class="action-card">
  <b>🔍 Buscar</b> — Use el campo de búsqueda o los filtros disponibles.
</div>
```

### Tutorial Content Sections

A tutorial **must** include the following sections. The section names shown here are conceptual — use appropriate labels in the project's target language.

1. **📖 What is this view?** — Purpose, role in workflow, where it fits. *(e.g., "¿Qué es esta vista?")*
2. **📋 Columns / Fields** — Table: column name, description, editable (✅/—). *(e.g., "Columnas / Campos")*
3. **🏷️ Catalogs and statuses** — Predefined values with descriptions (if applicable). *(e.g., "Catálogos y estados")*
4. **🔍 Available filters** — What filters exist, default values. *(e.g., "Filtros disponibles")*
5. **🚶 Step-by-step flow** — Step cards for create/edit/use flow. *(e.g., "Flujo paso a paso")*
6. **⚠️ Considerations** — Restrictions, warnings, related views (use tip boxes). *(e.g., "Consideraciones")*
7. **📚 Glossary** — Domain terms used in this view (use glossary grid or table). *(e.g., "Glosario")*

### Context Content Sections

A context file **must** include the following sections. Use appropriate labels in the project's target language.

1. **⚡ Quick actions** — Action cards for each available operation. *(e.g., "Acciones rápidas")*
2. **📋 Columns** — Compact table (column, description, editable). *(e.g., "Columnas")*
3. **🏷️ Statuses / key values** — Summary of enumerations or statuses. *(e.g., "Estados / valores clave")*
4. **💡 Notes** — Tips and warnings (use tip boxes). *(e.g., "Notas")*

---

## Module Manual — Sidebar Layout

The module manual is a standalone document with a fixed sidebar for navigation.

### Layout

```
┌──────────┬────────────────────────────────┐
│          │  Top Bar (sticky)              │
│ Sidebar  ├────────────────────────────────│
│ (260px)  │                                │
│ fixed    │  Content (max-width: 960px)    │
│          │                                │
│ - Logo   │  Section 1                     │
│ - Nav    │  Section 2                     │
│   links  │  ...                           │
│          │                                │
└──────────┴────────────────────────────────┘
```

### Manual-Only Components

#### Phase Cards (`.phase-card`)

Color-coded cards for process phases:

```html
<div class="phase-card p1">
  <span class="phase-number" style="background:rgba(191,96,255,0.2);color:#bf60ff">
    Fase 1 · Pedido
  </span>
  <h3>Descripción de la fase</h3>
  <ul class="checklist">
    <li>☐ Paso o requisito</li>
  </ul>
</div>
```

#### Alert Rows (`.alert-row`)

For documenting system alerts by severity:

```html
<div class="alert-row roja">
  <span class="alert-icon">🔴</span>
  <div class="alert-content">
    <strong>Título de la alerta</strong>
    <span><strong>Cuándo:</strong> Condición de activación.<br>
    <strong>Acción:</strong> Lo que el usuario debe hacer.</span>
  </div>
</div>

<div class="alert-row amarilla"><!-- 🟡 Planning alerts --></div>
<div class="alert-row azul"><!-- 🔵 Operational alerts --></div>
```

#### Actor Cards (`.actors-grid`)

```html
<div class="actors-grid">
  <div class="actor-card">
    <span class="actor-icon">🏭</span>
    <div class="actor-name">Proveedor</div>
    <div class="actor-desc">Descripción del rol.</div>
  </div>
</div>
```

#### Checklist (`.checklist`)

```html
<ul class="checklist">
  <li>☐ Paso con <strong>términos clave</strong> resaltados.</li>
</ul>
```

#### Mermaid Diagrams (optional)

```html
<div class="diagram-wrapper">
  <div class="mermaid">
    sequenceDiagram
      participant A as Actor
      ...
  </div>
</div>
```

#### Acronym Tooltips (optional)

Auto-replaces known acronyms with hover tooltips via JS text walker.

### Sidebar Navigation

```html
<nav id="sidebar">
  <div class="logo">
    <strong>📦 AppName</strong>
    <span>Manual — Nombre del Módulo</span>
  </div>
  <nav>
    <div class="section-label">Introducción</div>
    <a href="#sec-intro">📖 ¿Qué es este módulo?</a>
    <a href="#sec-actors">👥 Actores del proceso</a>

    <div class="section-label">Proceso</div>
    <a href="#sec-phases">🗺️ Las N Fases</a>
    <a href="#sec-f1"><span class="phase-dot" style="background:#color"></span> Fase 1 — Nombre</a>

    <div class="section-label">Vistas y Herramientas</div>
    <a href="#sec-view1">📋 Nombre de Vista</a>

    <div class="section-label">Referencia</div>
    <a href="#sec-alerts">⚠️ Guía de Alertas</a>
    <a href="#sec-glossary">📚 Glosario</a>
    <a href="#sec-faq">❓ Preguntas Frecuentes</a>
  </nav>
</nav>
```

Include JavaScript for active-link-on-scroll using `IntersectionObserver`.

### Manual Content Sections

A module manual **must** include the following sections. The section names shown here are conceptual — use appropriate labels in the project's target language.

1. **📖 Introduction** — What the module does, problem it solves, how to access it, entity hierarchy. *(e.g., "Introducción")*
2. **👥 Actors** — All participants (internal, external, system) with actor cards. *(e.g., "Actores")*
3. **🗺️ Process phases** — Overview table: phase, name, summary, trigger. *(e.g., "Fases del proceso")*
4. **Per-Phase Detail** — One section per phase with phase card, checklist, alerts, key dates.
5. **📋 Views and tools** — Specialized views (Pipeline, Dashboard), how they relate to the flow. *(e.g., "Vistas y herramientas")*
6. **📚 Domain concepts** — Complex business concepts, comparison tables (if applicable). *(e.g., "Conceptos del dominio")*
7. **⚠️ Alert guide** — All system alerts grouped by severity with alert-row components. *(e.g., "Guía de alertas")*
8. **📚 Glossary** — All domain terms using glossary-grid. *(e.g., "Glosario")*
9. **❓ FAQ** — 5-10 FAQ items in accordion format. *(e.g., "Preguntas frecuentes")*

### Print Support

Include `@media print` rules to hide sidebar and make top-bar static.

---

## Discovery & FAB Behavior

1. The backend registers a `HelpDocsService` subclass via `registerService` (see [Configuration](#configuration) above), and the client registers the proxy via `HelpDocsServiceRegistry`.
2. The fsLib base `View` class queries the service automatically using the view's class name + `helpModule.slug`. Discovery can be disabled per-view by overriding `helpEnabled`:
   ```kotlin
   // Disable help discovery for a specific view
   override val helpEnabled: Boolean = false
   ```
3. If help files exist for the view, a floating **"?" dropdown button** appears (fixed, bottom-right corner, hover-triggered) — no extra config needed.
4. The dropdown offers up to two items:
   - **View Help** — Opens an **offcanvas panel** (slide-in from the right). If both tutorial and context files exist, the panel includes **tabs** to switch between them. If only one type exists, the panel shows it directly (no tabs). Content is lazy-loaded on first access. A "detach" button allows opening the content in a separate browser window.
   - **Module Manual** — Opens a **modal dialog** (extra-large) with the manual loaded in an iframe. An "Open in separate window" button allows detaching it to a full browser window.

   Tutorial and context are **never** shown alongside the manual — they use separate UI containers (offcanvas vs. modal).

---

## Naming Conventions

| Prefix | View type | Example |
|--------|-----------|---------|
| `ViewList*` | List view (table with filters) | `ViewListImportProject` |
| `ViewItem*` | Detail / form view | `ViewItemImportProject` |
| `View*` (other) | Dashboard, pipeline, special | `ViewImportDashboard` |

Each List/Item pair should have its own tutorial + context.

## Emoji Icon Reference

Use emojis consistently as visual anchors:

| Emoji | Usage |
|-------|-------|
| 📖 | Introduction, "what is this" |
| 📋 | Columns, tables, lists, views |
| 📌 | Steps, pinned items, key terms |
| 🔍 | Search, filters |
| ➕ | Create action |
| ✏️ | Edit action |
| 🗑️ | Delete action |
| 👥 | Actors, users, roles |
| 🗺️ | Process map, phases |
| ⚡ | Quick actions, context help |
| 💡 | Tips, info |
| ⚠️ | Warnings |
| 🚨 | Danger, critical |
| ✅ | Success, confirmed |
| 📚 | Glossary, reference |
| ❓ | FAQ |
| 🏷️ | Tags, categories, statuses |
| 🔔 | Alerts, notifications |
| 📊 | Dashboard, analytics |
| 🚶 | Step-by-step flow |
| 🔗 | Related views, links |

## Content Tone & Language

- **Keep it enjoyable** — Help content should be pleasant to read, not dry or robotic. Use a friendly, conversational tone. Add light humor or playful remarks where appropriate to keep users engaged and make the learning experience fun.
- Content language is determined by each downstream project — write help files in whichever language your users expect
- Use **direct UTF-8 characters** for all accented or special characters (e.g., á, é, ñ, ü, ¿, —)
- Do NOT use HTML entities for accented characters
- Use `<strong>` for key terms, `<em>` for emphasis
- Use `<code>` for system identifiers (model names, field names)

## Checklist

### For a Module Manual
1. Identify the module and its business process
2. List all actors/participants
3. Map the process phases with trigger events
4. Document each phase with system steps (checklist)
5. Identify all system alerts and their triggers
6. Collect domain terms for the glossary
7. Write 5-10 FAQ items
8. Build sidebar navigation matching all sections
9. Apply dark theme CSS (copy from existing manual)
10. Add Mermaid diagram if process has complex interactions
11. Test print output
12. Link the manual from the module's landing view

### For Tutorial + Context
1. Identify the exact class name of the view
2. Create the directory `help-docs/{module-slug}/{ViewClassName}/`
3. Read the view's source code to understand columns, filters, actions, flow
4. Create `tutorial.html` with all 7 required sections
5. Create `context.html` with all 4 required sections
6. Apply dark theme CSS with emoji icons
7. Write all content in the project's target language (UTF-8, no HTML entities)
8. Verify directory name matches the class name exactly
