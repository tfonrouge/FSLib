# CONTRACT — view-consumer-gaps

Durable invariants this wave establishes, and the new public API surface (this file carries the
API_SURFACE duty per LEDGER L-002). KDoc on the symbols references this contract.

## Invariants

- **I-1 (in-flight guard).** `FsTomSelectRemoteInput.refreshState()` never starts a second
  options load for a value while a load for that same value is in flight; the dropped calls'
  outcome is delivered by the in-flight load's completion callback (which runs the base refresh).
- **I-2 (failure clears the guard).** A load that fails (RPC error) clears the in-flight marker,
  so a later `refreshState()` can retry — the guard never turns a transient failure into a
  permanently stale selector. This preserves upstream's retry-on-next-refresh semantics.
- **I-3 (behavioral mirror + retirement).** Apart from I-1/I-2, `FsTomSelectRemote` /
  `FsTomSelectRemoteInput` behave identically to KVision's `TomSelectRemote` /
  `TomSelectRemoteInput` (same constructor surface, same FormControl contract), so a consumer
  swap is import + builder-name only. The wrapper carries L-003's retirement condition:
  **at every KVision version bump, re-read `TomSelectRemoteInput.refreshState` sources; when
  upstream guards in-flight loads, retire these classes.**
- **I-4 (translatable strings).** Every user-facing string introduced or touched by this wave
  (`ViewItem` cancel dialog; `helpButtons.kt`) resolves through KVision i18n — **`I18n.tr` lazy
  markers only where a component's whole text is exactly the key; immediate `gettext` for every
  interpolated or raw-DOM string** (a mid-string marker either swallows the suffix into the
  lookup key or leaks raw — L-008/ACS-05). English source keys for new strings; enum labels wrapped at
  point of use — `HelpType`'s are Spanish, `HelpTheme`'s are English (L-005; the "both Spanish"
  wording was corrected in L-010).
- **I-5 (late-bound help titles).** The help offcanvas caption, manual modal caption, and
  detached-window title are computed when the user opens them, never captured at FAB
  construction; the `String` overload of `helpButtons` delegates to the provider overload, and
  **fsLib's own caller (`View.startDisplayPage`) passes a live provider** so standard views get
  the fix without any consumer change (L-008/ACS-04).

## New public API (additive — SemVer minor)

| Symbol | Kind | Module / package | Notes |
|---|---|---|---|
| `FsTomSelectRemoteInput<T>` | open class (extends `TomSelectRemoteInput<T>`) | `fullstack` jsMain · `com.fonrouge.fullStack.form` | I-1/I-2 guard; constructor mirrors KVision's |
| `FsTomSelectRemote<T>` | open class (`StringFormControl`, mirror of `TomSelectRemote<T>`) | `fullstack` jsMain · `com.fonrouge.fullStack.form` | KVision hard-wires its input (`final override val`), hence a mirror, not a subclass |
| `Container.fsTomSelectRemote(...)` | DSL builder | same | drop-in for `tomSelectRemote(...)` |
| `Container.fsTomSelectRemoteInput(...)` | DSL builder | same | drop-in for `tomSelectRemoteInput(...)` |
| `Container.helpButtons(viewClassName, viewLabelProvider: () -> String, moduleSlug, showTutorial)` | overload | `fullstack` jsMain · `com.fonrouge.fullStack.layout` | carries the implementation; `String` overload delegates |

`InFlightValueGuard` is **internal** — an implementation detail of `FsTomSelectRemoteInput`
(unit-tested in-module), deliberately not part of the public surface (L-008/ACS-06).

## Consumer migration notes (for the release's CHANGELOG/MIGRATION entry)

- Swap `tomSelectRemote(...)` → `fsTomSelectRemote(...)` (and the `Input` variant) to get I-1/I-2.
  **Qualified (L-010/ACS-03)**: inferred-type DSL call sites change import + builder name only; a
  variable, parameter, or receiver explicitly typed as KVision's `TomSelectRemote` needs adapting —
  `FsTomSelectRemote` is a sibling class, not a subclass.
- **Runtime-visible for existing consumers even without the swap**: previously hardcoded help-UI
  strings become English i18n keys; without catalog entries the help UI shows English. Exact
  15-key checklist (shipped in MIGRATION.md 6.2.4 → 6.3.0): `Please Confirm`,
  `Cancel and forget current changes?`, `Yes`, `No`, `Help`, `View Help`,
  `Open in separate window`, `Separate window`,
  `Browse the manual here or open it in a separate window.`, `Tutorial`, `Ayuda Contextual`,
  `Manual del Módulo`, `Auto (OS)`, `Dark`, `Light`.
- `helpButtons`: fsLib's standard views are already fixed (`View.startDisplayPage` passes a live
  provider). A consumer calling `helpButtons` directly with a label that loads after construction
  should use the provider overload; the `String` overload necessarily keeps a fixed label
  (unchanged behavior).
