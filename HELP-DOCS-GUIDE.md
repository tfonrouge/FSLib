# Help System — Unified Build Guide

> **Version:** 1.2.0

## Overview

The help system provides three types of documentation, all sharing a **dark theme** with emoji icons and visual components:

| Type | File | Scope | Audience | Layout |
|------|------|-------|----------|--------|
| 📦 **Module Manual** | `manual.html` | Entire module | Anyone learning the business process | Sidebar + scrollable content |
| 📖 **Tutorial** | `tutorial.html` | Business task (may span multiple views) | User learning to perform a specific task step by step | Panel (no sidebar) |
| ⚡ **Context Help** | `context.html` | Single view | Any user needing a reference about the view | Panel (no sidebar) |

### What is a Tutorial?

A tutorial is a **teaching guide** designed to help someone learn to perform a specific task step by step. It is the bridge between *"I don't know how to do this"* and *"I've done it"*.

A true tutorial must have these characteristics:

- **Sequentiality**: Follows a logical order (Step 1, Step 2...) that the user must complete in sequence.
- **Clear objective**: Focuses on a concrete end result (e.g., "How to register a new import project").
- **Interactivity**: The user actively follows along, performing each action as they read — not just passively reading.
- **Instructive language**: Uses action verbs throughout (click, select, type, navigate, confirm).

A tutorial is **not** a reference document. It does not describe the interface — it guides the user through it.

**Cross-view scope**: A tutorial may span multiple views when the business task requires it. For example, a tutorial for "How to register a work order" might start in `ViewListWorkOrder` (click **+**), continue in `ViewItemWorkOrder` (fill fields, save), and return to the list (verify result). The tutorial file lives in the directory of the view where the task **begins** (e.g., `ViewListWorkOrder/tutorial.html`), but its steps can guide the user through any views involved in completing the task.

**Detached window for cross-view tutorials**: Since the in-app help panel is tied to the active view and closes on navigation, cross-view tutorials work best when **opened in a separate browser window** (using the existing "detach" button). This lets the user follow the steps while freely navigating between views. When writing a cross-view tutorial, include a tip at the beginning recommending the user to detach the tutorial window before starting. Use clear navigation instructions in each step (e.g., "Navigate to **Workshop → Operators** in the sidebar") — the user knows their own application and can follow along.

### What is Context Help?

Context help is a **view reference card** — a comprehensive description of everything a user needs to know about a specific screen. It covers what the view is, what actions are available, what each column/field means, and important considerations. It serves both first-time users exploring the interface and experienced users who need a quick reminder.

## Directory Structure

```
app-module/
└── help-docs/
    ├── {module-slug}/                    ← e.g. taller/
    │   ├── manual.html                   ← Module manual (sidebar layout)
    │   ├── ViewListEntity/
    │   │   ├── tutorial.html             ← Cross-view tutorial (includes _fields.html)
    │   │   └── context.html
    │   ├── ViewItemEntity/
    │   │   ├── tutorial.html             ← Form tutorial (includes _fields.html)
    │   │   ├── context.html
    │   │   └── _fields.html              ← Shared form fields fragment
    │   └── ...
    ├── {another-module}/
    │   ├── manual.html
    │   └── ...
    └── ViewWithoutModule/                ← Views not assigned to a module
        ├── tutorial.html
        └── context.html
```

### Shared Fragments (`_fields.html`)

When a ViewList tutorial crosses into a ViewItem and describes the same form fields that the ViewItem tutorial covers, the field descriptions should be written **once** in a shared HTML fragment to avoid duplication.

**Convention**: The fragment file is named `_fields.html` (prefixed with underscore to distinguish it from served help types) and lives in the **ViewItem's directory**, since the form belongs to that view. Both tutorials include it via a server-side include directive.

**Fragment file** — `ViewItemEntity/_fields.html`:

Contains only the form field steps (no `<html>`, `<head>`, or `<body>` tags). It is a raw HTML fragment that the server injects at build time:

```html
<!-- _fields.html — Shared form field descriptions for ViewItemEntity -->

<h3>Part A — Header Fields</h3>

<div class="step-card">
  <div class="step-title">📌 Step 1 — Select the equipment</div>
  <p>In the <strong>Equipment</strong> field, search and select the equipment
  that needs maintenance. The equipment type determines which operations
  are available in the work route.</p>
</div>

<div class="step-card">
  <div class="step-title">📌 Step 2 — Set the priority</div>
  <p>Choose the <strong>Priority</strong> level. <span class="tag tag-red">Urgent</span>
  orders are highlighted in the list and trigger notifications to supervisors.</p>
</div>

<!-- ... more steps ... -->

<h3>Part B — Detail List (Work Route)</h3>

<div class="step-card">
  <div class="step-title">📌 Step N — Add operations to the route</div>
  <p>Click <strong>+</strong> in the operations table to add a new step.
  Select the <strong>Operation</strong> and assign an <strong>Operator</strong>.</p>
</div>
```

**Including the fragment** — Use an HTML comment directive. The `HelpDocsService` backend resolves these **server-side** before sending the content to the client, so no client-side JavaScript is needed.

From the ViewList tutorial (`ViewListEntity/tutorial.html`):

```html
<!-- Where the form field steps should appear -->
<!-- include: ../ViewItemEntity/_fields.html -->
```

From the ViewItem's own tutorial (`ViewItemEntity/tutorial.html`):

```html
<!-- include: _fields.html -->
```

Paths are resolved **relative to the directory of the file containing the directive**. The `../` syntax works for referencing files in sibling view directories.

**How it works**: When `getHelpContent()` reads an HTML file, it scans for `<!-- include: path -->` directives and replaces each one with the content of the referenced file. Includes are resolved recursively (up to 5 levels deep) and are sandboxed to the `help-docs` root directory for security.

**Important**: The `_fields.html` file is **not** a help type — the backend's `getAvailableHelp()` only looks for `tutorial.html`, `context.html`, and `manual.html`. Files prefixed with underscore are ignored by the discovery system.

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
import com.fonrouge.base.enums.HelpTheme
import dev.kilua.rpc.getService

HelpDocsServiceRegistry.service = getService<IHelpDocsService>()

// Optional — override the help theme (default is AUTO, which follows OS preference):
HelpDocsServiceRegistry.theme = HelpTheme.LIGHT
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

## Theme Support

All three file types support **dark** and **light** color themes. The theme is controlled by the `data-help-theme` attribute that FSLib injects automatically on the help content container. Help HTML files should define both palettes using this attribute.

### Theme Modes

- **`auto`** (default): Follows the user's OS/browser `prefers-color-scheme` preference.
- **`dark`**: Forces the dark color scheme.
- **`light`**: Forces the light color scheme.

Consumer apps can override the default via `HelpDocsServiceRegistry.theme = HelpTheme.LIGHT` (see [Configuration](#configuration)).

### CSS Variables

Define both themes in your help files. The dark palette is the default; the light palette overrides it when the appropriate `data-help-theme` value is active.

```css
/* === Dark theme (default) === */
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
  --text-dim: #c8cde6;

  /* Accent colors */
  --accent-blue: #38bdf8;
  --accent-purple: #7f6bff;
  --accent-green: #1ed8a4;
  --accent-yellow: #f5b43f;
  --accent-red: #e05252;
  --accent-cyan: #00e5e5;
}

/* === Light theme === */
body[data-help-theme="light"] {
  --bg: #ffffff;
  --bg-card: #f4f6fa;
  --bg-sidebar: #f0f2f8;

  --border: #d0d5dd;
  --border-light: #e2e6ed;

  --text: #1a1a2e;
  --text-muted: #5a6178;
  --text-dim: #4a5068;

  --accent-blue: #0b7dda;
  --accent-purple: #5b4cc4;
  --accent-green: #0f9d76;
  --accent-yellow: #c78c1e;
  --accent-red: #c43c3c;
  --accent-cyan: #0a8f8f;
}

/* === Auto theme (follows OS preference) === */
@media (prefers-color-scheme: light) {
  body[data-help-theme="auto"] {
    --bg: #ffffff;
    --bg-card: #f4f6fa;
    --bg-sidebar: #f0f2f8;

    --border: #d0d5dd;
    --border-light: #e2e6ed;

    --text: #1a1a2e;
    --text-muted: #5a6178;
    --text-dim: #4a5068;

    --accent-blue: #0b7dda;
    --accent-purple: #5b4cc4;
    --accent-green: #0f9d76;
    --accent-yellow: #c78c1e;
    --accent-red: #c43c3c;
    --accent-cyan: #0a8f8f;
  }
}
```

**Important**: When help content is displayed inside the offcanvas panel, FSLib scopes `body` selectors to `.help-content-wrap`. This means `body[data-help-theme="light"]` automatically becomes `.help-content-wrap[data-help-theme="light"]` — no extra work is needed. The theme attribute is set on both the wrapper div (offcanvas) and the `<body>` tag (detached windows and manual iframe).

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
  color: var(--text-dim);
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
    /* === Dark theme (default) === */
    :root {
      --bg: #0d1117;
      --bg-card: #111633;
      --border: #22295a;
      --border-light: #2a2f5f;
      --text: #e8ebff;
      --text-muted: #9da4d1;
      --text-dim: #c8cde6;
      --accent-blue: #38bdf8;
      --accent-purple: #7f6bff;
      --accent-green: #1ed8a4;
      --accent-yellow: #f5b43f;
      --accent-red: #e05252;
      --accent-cyan: #00e5e5;
    }
    /* === Light theme === */
    body[data-help-theme="light"] {
      --bg: #ffffff; --bg-card: #f4f6fa;
      --border: #d0d5dd; --border-light: #e2e6ed;
      --text: #1a1a2e; --text-muted: #5a6178; --text-dim: #4a5068;
      --accent-blue: #0b7dda; --accent-purple: #5b4cc4; --accent-green: #0f9d76;
      --accent-yellow: #c78c1e; --accent-red: #c43c3c; --accent-cyan: #0a8f8f;
    }
    /* === Auto theme (follows OS preference) === */
    @media (prefers-color-scheme: light) {
      body[data-help-theme="auto"] {
        --bg: #ffffff; --bg-card: #f4f6fa;
        --border: #d0d5dd; --border-light: #e2e6ed;
        --text: #1a1a2e; --text-muted: #5a6178; --text-dim: #4a5068;
        --accent-blue: #0b7dda; --accent-purple: #5b4cc4; --accent-green: #0f9d76;
        --accent-yellow: #c78c1e; --accent-red: #c43c3c; --accent-cyan: #0a8f8f;
      }
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
      color: var(--text-dim);
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
      color: var(--text-dim);
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

Use step cards for each step of the tutorial. Each step should tell the user **what to do**, **where to do it**, and **what result to expect**.

```html
<div class="step-card">
  <div class="step-title">📌 Step 1 — Access the view</div>
  <p>Navigate to the <strong>Imports</strong> module in the sidebar and click <strong>Projects</strong>. The project list will appear with all active records.</p>
</div>

<div class="step-card">
  <div class="step-title">📌 Step 2 — Create a new project</div>
  <p>Click the <strong>+</strong> button in the toolbar. A form will open with the required fields highlighted.</p>
</div>
```

#### Expected Result Box (`.result-box`)

Use at the end of the tutorial to show what the user should see when they finish successfully.

```html
<div class="result-box">
  <div class="result-title">✅ Expected result</div>
  <p>The new project appears in the list with status <span class="tag tag-green">Active</span> and today's date.</p>
</div>
```

```css
.result-box {
  background: rgba(30,216,164,0.08);
  border: 1px solid var(--accent-green);
  border-radius: 8px;
  padding: 16px 20px;
  margin: 16px 0;
}
.result-title {
  font-weight: 700;
  color: var(--accent-green);
  margin-bottom: 6px;
  font-size: 0.95em;
}
.result-box p { color: var(--text-dim); }
```

### Context-Specific Components

#### Action Cards

```html
<div class="action-card">
  <b>➕ Create record</b> — <b>+</b> button in the toolbar.
</div>

<div class="action-card">
  <b>🔍 Search</b> — Use the search field or available filters.
</div>
```

### Tutorial Content Sections

A tutorial is a **step-by-step teaching guide** that walks the user through a specific task from start to finish. It uses instructive language (action verbs: click, select, type, navigate) and follows a sequential flow the user performs in real time.

A tutorial **must** include the following sections. The section names shown here are conceptual — use appropriate labels in the project's target language.

1. **🎯 Objective** — One clear sentence stating what the user will accomplish by the end of this tutorial. *(e.g., "Objetivo")*
2. **📋 Prerequisites** — What the user needs before starting: permissions, prior data, previous steps completed. If none, state it explicitly. *(e.g., "Prerrequisitos")*
3. **🚶 Steps** — The core of the tutorial. A sequential series of step cards, each with: what action to perform, where to perform it (which button, field, menu), and what happens as a result. Use instructive language throughout. The structure of the steps varies by view type (see below). *(e.g., "Pasos")*
4. **✅ Expected result** — How to verify the task was completed successfully. Use the result-box component. *(e.g., "Resultado esperado")*
5. **💡 Next steps** — What the user can do after completing this tutorial: related views, follow-up actions, advanced features. *(e.g., "Siguientes pasos")*

#### Steps structure by view type

**ViewList tutorials** — guide a **business task** that starts in the list and may cross into other views (ViewItem, other ViewLists, etc.). Steps follow a navigation flow: open the list, create/select a record, fill data in the detail view, return to verify.

**Cross-view tip**: If the tutorial spans multiple views, include a tip-info box right after the objective recommending the user to open the tutorial in a separate window:

```html
<div class="tip tip-info">
  <span class="tip-icon">💡</span>
  <p><strong>Tip.</strong> This tutorial spans multiple screens. Click the
  <strong>detach</strong> button (top-right corner) to open it in a separate
  window so you can follow along while navigating the application.</p>
</div>
```

**ViewItem tutorials (create/edit)** — guide the user through **completing the form** that is currently on screen. Since the user is already looking at the form, the steps should explain what to fill, in what order, and why. Structure the steps in two parts when the view has both header fields and an embedded detail list:

- **Part A — Header fields**: Walk through each field or group of related fields in the order they appear. For each step, explain: what the field is for, what to select/type, and any dependencies between fields (e.g., "selecting the equipment type determines the available operations").
- **Part B — Detail list**: Explain how to add items to the embedded list (e.g., products on an invoice, steps on a work route). Cover: how to add a row, what each column means, how to edit/remove rows, and any automatic calculations (e.g., totals, sequences).

If the view has only header fields (no embedded list), Part B is omitted. Use tip boxes for validations, business rules, and common mistakes within each step.

**Shared fragment**: The form field descriptions (Part A + Part B) should be written in a `_fields.html` fragment (see [Shared Fragments](#shared-fragments-_fieldshtml)) and included via `<!-- include: _fields.html -->` so the same content can be reused by both the ViewList cross-view tutorial and the ViewItem tutorial without duplication.

Example structure for an invoice ViewItem tutorial:

```
🎯 Objective: Complete a new purchase invoice with its product lines.

📋 Prerequisites: Supplier and products must be registered.

🚶 Steps:
  [shared-fields loaded from _fields.html]:
    Part A — Invoice Header
      Step 1 — Select the supplier (explains the field, what happens when selected)
      Step 2 — Set the invoice date and due date (explains defaults, constraints)
      Step 3 — Choose the payment terms (explains options and their effect)
      ⚠️ Tip: The currency is determined by the supplier and cannot be changed here.

    Part B — Product Lines
      Step 4 — Add the first product line (click +, select product, set quantity)
      Step 5 — Adjust price if needed (explains when override is allowed)
      Step 6 — Repeat for additional products
      💡 Tip: The total updates automatically as you add lines.

  Step 7 — Review and save (what validations run, what happens on save)

✅ Expected result: Invoice appears with status "Pending" and correct total.

💡 Next steps: How to approve the invoice, how to link it to a purchase order.
```

### Tutorial Visibility in Detail Views

Tutorials are automatically **hidden** when a `ViewItem` opens in read-only detail mode (`CrudTask.Read`). The system only shows tutorials in create/edit mode, where step-by-step guidance is relevant. This is handled by the `showTutorial` parameter in the `helpButtons()` function — no additional configuration is needed.

In summary:
- **ViewList**: Tutorial always available (if file exists)
- **ViewItem in Create/Edit mode**: Tutorial available
- **ViewItem in Read-only mode**: Tutorial hidden, only context help and manual shown

### Context Content Sections

A context file is a **view reference card** that describes everything about the view: what it is, what actions are available, what each column means, and important notes. It serves as both introduction and quick reference.

A context file **must** include the following sections. Use appropriate labels in the project's target language.

1. **📖 What is this view?** — Purpose, role in workflow, where it fits. *(e.g., "¿Qué es esta vista?")*
2. **⚡ Quick actions** — Action cards for each available operation (toolbar buttons). *(e.g., "Acciones rápidas")*
3. **🖱️ Row context menu** *(list views only)* — Options available in the right-click context menu on each row (view detail, edit, delete, domain-specific actions). *(e.g., "Menú contextual")*
4. **📋 Columns / Fields** — Table: column name, description, editable (✅/—). *(e.g., "Columnas / Campos")*
5. **🏷️ Catalogs and statuses** — Predefined values with descriptions (if applicable). *(e.g., "Catálogos y estados")*
6. **🔍 Available filters** *(list views only)* — What filters exist, default values. *(e.g., "Filtros disponibles")*
7. **💡 Notes and considerations** — Tips, warnings, restrictions, related views (use tip boxes). *(e.g., "Notas y consideraciones")*
8. **📚 Glossary** — Domain terms used in this view (use glossary grid or table). *(e.g., "Glosario")*

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

Each List/Item pair should have its own context help. Tutorials are optional and should be created when a view involves a task that benefits from guided step-by-step instruction.

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
| 🖱️ | Context menu, right-click actions |
| 🎯 | Objective, goal |
| 📊 | Dashboard, analytics |
| 🚶 | Step-by-step tutorial flow |
| 🔗 | Related views, links, next steps |

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

### For Tutorial + Context (ViewList / ViewItem pair)
1. Identify the exact class names of both views (ViewList and ViewItem)
2. Create directories `help-docs/{module-slug}/{ViewListClassName}/` and `help-docs/{module-slug}/{ViewItemClassName}/`
3. Read the view source code to understand columns, filters, actions, form fields, and flow
4. Create `_fields.html` in the ViewItem directory — shared form field descriptions (Part A + Part B)
5. Create `tutorial.html` for the ViewList — cross-view business task, includes `_fields.html` via fetch
6. Create `tutorial.html` for the ViewItem — form completion guide, includes `_fields.html` via fetch (remember: tutorials are hidden in read-only detail views)
7. Create `context.html` for both views — comprehensive view reference card with all 8 required sections (include row context menu for list views)
8. Apply dark theme CSS with emoji icons
9. Write all content in the project's target language (UTF-8, no HTML entities)
10. Verify directory names match the class names exactly
8. Verify directory name matches the class name exactly
