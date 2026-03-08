# Help Docs - Build Guide

## General Description

The online help system displays contextual documentation to the end user within the application. Each view (View) can have associated help files that are automatically discovered by the `IHelpDocsService` service. When help files exist for a view, a floating action button (FAB) appears in the interface.

## For Claude Code / AI Agents

When you start working on a project that uses this help system, **ask the user** for the location of the `help-docs/` directory if it is not documented in the project's `CLAUDE.md`. Save the answer in your memory for future sessions.

## `help-docs/` Directory Location

Help files are created inside a `help-docs/` directory typically located in the **application module** (the module containing the backend `Main.kt`). This is the recommended location because it keeps the documentation alongside the code that serves it. The exact location may vary by project.

```
my-project/
├── app-module/                    ← main application module
│   ├── src/
│   ├── build.gradle.kts
│   └── help-docs/                 ← help directory (inside the app module)
│       ├── ViewListEntity/
│       │   ├── tutorial.html
│       │   └── context.html
│       ├── ViewItemEntity/
│       │   ├── tutorial.html
│       │   └── context.html
│       └── ...
├── lib-module/                    ← shared library module
├── build.gradle.kts
├── settings.gradle.kts
└── ...
```

### Configuration in `Main.kt`

The default directory is `help-docs/`. If the project needs to use a different name, it can be changed with `HelpDocsService.setHelpDocsDir()`:

```kotlin
HelpDocsService.setHelpDocsDir("docs/help")  // only if a different name than the default is needed
```

> **Note:** There is no need to call `setHelpDocsDir()` if the default name `help-docs` is used.

## File Structure per View

Each view has its own subdirectory inside `help-docs/` with up to two files:

```
help-docs/{ViewClassName}/
├── tutorial.html      ← Tutorial (step-by-step guide)
└── context.html       ← Contextual help (quick reference)
```

- The subdirectory name **must match exactly** the class name of the frontend view (e.g., `ViewListClient`, `ViewItemWorkOrder`).

## Help Types

### `tutorial.html` — Tutorial

A comprehensive guide aimed at someone who has **never used** the view. It should cover:

1. **What the view is** — purpose and role within the application workflow.
2. **Columns / fields** — table describing each column or field, what it displays, and whether it is editable.
3. **Catalogs and enumerations** — if the view uses predefined values (levels, statuses, types), list them with their description.
4. **Available filters** — what filters the view offers, default values.
5. **Step-by-step flow** — sequential instructions to create, edit, or use the main functionality.
6. **Important considerations** — restrictions, warnings, relationships with other views.
7. **Glossary** — domain terms used in the view.

### `context.html` — Contextual Help

A quick reference "cheat sheet" for someone who **already knows** the view but needs a reminder. It should include:

1. **Quick actions** — what the user can do on this screen (brief action cards).
2. **Columns** — compact table with column, description, and whether it is editable.
3. **Statuses / key values** — summary table of levels, statuses, or enumerations.
4. **Notes and tips** — brief reminders about restrictions or shortcuts.

## Required HTML Format

Both files are complete HTML (`<!DOCTYPE html>`) with embedded styles. The following conventions must be followed:

### Base structure

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{Type} &mdash; {View Name}</title>
    <style>
        /* Embedded styles (see styles section) */
    </style>
</head>
<body>
<div class="ctx">
    <!-- Content here -->
</div>
</body>
</html>
```

### Standard styles

Copy the styles from the `<style>` block of any existing file in `help-docs/`. The key styles are:

| CSS Class         | Usage                                                    |
|-------------------|----------------------------------------------------------|
| `.ctx`            | Main container for all content.                          |
| `.ctx h1`         | Main title (blue, centered).                             |
| `.ctx .subtitle`  | Gray centered subtitle below h1.                         |
| `.ctx h2`         | Sections (blue with left border).                        |
| `.ctx h3`         | Subsections.                                             |
| `.ctx table`      | Tables with blue header and alternating rows.            |
| `.shortcut`       | Step card in tutorials (light blue background).          |
| `.action-card`    | Quick action card in contextual help.                    |
| `.tip`            | Tip box (left blue border, light blue background).       |
| `.warn`           | Warning box (yellow border, yellow background).          |

### Special character encoding

Use HTML entities for any non-ASCII characters and common symbols. Set the `lang` attribute
on the `<html>` tag to match the content language (e.g., `lang="en"`, `lang="es"`, `lang="pt"`).

| Character | Entity       | Description               |
|-----------|--------------|---------------------------|
| &aacute;  | `&aacute;`   | a with acute accent       |
| &eacute;  | `&eacute;`   | e with acute accent       |
| &iacute;  | `&iacute;`   | i with acute accent       |
| &oacute;  | `&oacute;`   | o with acute accent       |
| &uacute;  | `&uacute;`   | u with acute accent       |
| &ntilde;  | `&ntilde;`   | n with tilde              |
| &uuml;    | `&uuml;`     | u with diaeresis          |
| &iquest;  | `&iquest;`   | inverted question mark    |
| &check;   | `&check;`    | check mark                |
| &times;   | `&times;`    | multiplication sign       |
| &rarr;    | `&rarr;`     | right arrow               |
| &mdash;   | `&mdash;`    | em dash                   |
| &hellip;  | `&hellip;`   | horizontal ellipsis       |

## Naming Conventions

Views follow a consistent naming pattern:

| Prefix          | View type                              | Example                    |
|-----------------|----------------------------------------|----------------------------|
| `ViewList*`     | List view (table with filters)         | `ViewListClient`           |
| `ViewItem*`     | Detail / form view                     | `ViewItemClient`           |

Each List/Item pair of an entity should have its own help files.

## How Discovery Works

1. The backend configures `HelpDocsService.setHelpDocsDir("help-docs")` in `Main.kt`.
2. The `IHelpDocsService` service is registered as an RPC service.
3. The fsLib base `View` class queries the service automatically.
4. If help files exist for the view's class name, the FAB button appears.
5. No additional configuration is required — just create the files in the correct directory.

## Checklist for Creating Help Docs

1. Identify the exact class name of the view (e.g., `ViewListClient`).
2. Create the directory `help-docs/{ViewClassName}/` inside the configured directory.
3. Read the view's source code to understand columns, filters, actions, and flow.
4. Create `tutorial.html` with the complete step-by-step guide.
5. Create `context.html` with the quick reference.
6. Use the standard styles (copy from an existing file).
7. Write all content in the **project's target language** and set the `<html lang="...">` attribute accordingly.
8. Verify that the directory name matches the class name exactly.
