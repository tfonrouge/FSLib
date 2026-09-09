# PLAN — view-consumer-gaps

All items SAFE (additive API, no behavior change for untouched call sites). Order follows the
adopted value ranking T-3 → T-1 → T-4.

## Phase 1 — first wave · construction

| ID | Step | Advances | File anchors | Status |
|----|------|----------|--------------|--------|
| **P1.1** | `FsTomSelectRemoteInput` (in-flight guard per CONTRACT I-1/I-2, guard extracted as testable `InFlightValueGuard`) + `FsTomSelectRemote` mirror + `fsTomSelectRemote` / `fsTomSelectRemoteInput` builders. Note: the parent constructor's preset-value load dispatches into the subclass before its initializers run, so the guard field is `lateinit` (no initializer to wipe it) and created on first use — the constructor-phase load is guarded like every other (L-008/ACS-02); the guard tracks a SET of in-flight values so A→B→A value changes cannot re-fire an outstanding load (L-008/ACS-01); request params are prepared before marking so a throwing `stateFunction` cannot wedge a marker (L-008/ACS-03). | OC-01 | `fullstack/src/jsMain/.../form/FsTomSelectRemote.kt` (new) | ✅ done |
| **P1.2** | jsTest: guard state machine — a value already in flight is reported as such (the drop itself lives in `refreshState`, which consults the guard); completion and failure both clear; concurrent values tracked independently, incl. the A→B→A regression (L-008/ACS-01). | OC-01 | `fullstack/src/jsTest/.../form/InFlightValueGuardTest.kt` (new) | ✅ done (4 tests green) |
| **P1.3** | `ViewItem.backCloseAction` cancel dialog via immediate `gettext` (caption, text, yesTitle, noTitle) — texts provided by a testable internal provider. | OC-02 | `fullstack/src/jsMain/.../view/ViewItem.kt` (`confirmCancelTexts`) | ✅ done (via `gettext` — immediate translation; KVision's marker `tr` cannot interpolate) |
| **P1.4** | jsTest: the dialog text provider routes all four strings through immediate `gettext` (KVision i18n; observable with a stubbed manager or key-passthrough assertion). | OC-02 | `fullstack/src/jsTest/.../view/ConfirmCancelTextsTest.kt` (new) | ✅ done (2 tests green) |
| **P1.5** | `helpButtons` provider overload; captions computed at open/click; all user-facing strings through KVision i18n (`I18n.tr` whole-string markers / `gettext` interpolations); `String` overload delegates. | OC-03 | `fullstack/src/jsMain/.../layout/helpButtons.kt` | ✅ done (whole-string component labels use KVision's lazy marker `I18n.tr`; every interpolated string uses immediate `gettext` — a mid-string marker mistranslates or leaks) |
| **P1.6** | jsTest: caption provider timing — a viewLabel change after construction is reflected in the caption computed at open. | OC-03 | `fullstack/src/jsTest/.../layout/HelpCaptionTest.kt` (new) | ✅ done (2 tests green) |
| **P1.7** | T-2 / T-5 design entries recorded (LEDGER L-006/L-007); no implementation items. | OC-04 | `LEDGER.md` | ✅ done (at blueprint creation) |
| **P1.8** | Full build + jsBrowserTest green; TODO.md updated (T-1/T-3/T-4 point here; T-2/T-5 point to L-006/L-007). | OC-01..04 | `TODO.md` | ✅ done (full build + fullstack jsBrowserTest green 2026-09-08, 92 tests/0 failed; TODO.md annotated) |
| **P1.10** | ACS pre-commit advisory ACS-01..06 adopted and applied (guard rework, `View.startDisplayPage` provider wiring, offcanvas theme labels via `gettext`, records reconciled, `InFlightValueGuard` internal) — see LEDGER L-008. | OC-01..03 | `FsTomSelectRemote.kt`, `View.kt`, `helpButtons.kt`, blueprint records | ✅ done (2026-09-09) |
| **P1.11** | Second ACS round (L-009): detached-window title inserted as text (`document.title`/`textContent`, never through `document.write` — ACS-01); component-fixture attempt run and withdrawn per the pre-authorized fallback (uncaught RPC-failure errors poison unrelated Karma tests; standing oracles recorded in L-009 — ACS-02); PLAN mechanism wording swept (ACS-03). | OC-01..03 | `helpButtons.kt` (`detachToWindow`), `LEDGER.md` L-009 | ✅ done (2026-09-09) |
| **P1.9** | *(at next release)* CHANGELOG/MIGRATION entry per CONTRACT §Consumer migration notes; version per Owner (SemVer reading: minor). | OC-01..03 | `CHANGELOG.md`, `MIGRATION.md` | ☐ deferred to release |

## Deferred (no implementation this wave — OC-04)

| Task | Ledger | Reopens as |
|------|--------|-----------|
| T-2 Tabulator layout persistence (title/order freeze) | L-006 | PLAN item + DIRECTIVE amendment when the Owner picks option (a)/(b)/(c) |
| T-5 `openViewItem` close/write callback | L-007 | PLAN item + DIRECTIVE amendment when the Owner picks the callback semantics |

## Directive Completeness

| OC | Coverage set (Owner-approved obligations) | Verified | State | Assurance | Evidence |
|----|-------------------------------------------|----------|-------|-----------|----------|
| OC-01 | P1.1 + P1.2 + consumer 1-RPC measure (mppArel, `performance.getEntriesByType('resource')`) | 2/3 | IN PROGRESS | MIXED | P1.1 built (set guard, lateinit, param-prep order — L-008); `InFlightValueGuardTest` green incl. A→B→A regression (jsBrowserTest 2026-09-09); consumer measure pending — needs mppArel to adopt `fsTomSelectRemote` on a released fsLib |
| OC-02 | P1.3 + P1.4 | 2/2 | MET | REPLAYABLE | `ConfirmCancelTextsTest` green (stubbed I18nManager observes all four keys) |
| OC-03 | P1.5 + P1.6 + no-raw-string check on helpButtons.kt (literals AND `.label` interpolations) + `View.startDisplayPage` provider wiring | 4/4 | MET | REPLAYABLE | `HelpCaptionTest` green; widened grep clean 2026-09-09 (ACS-05 sites fixed); open-time wiring in the open/click handlers AND fsLib's own View caller (ACS-04) |
| OC-04 | L-006 + L-007 rows present; PLAN carries them only as deferred | 2/2 | MET | REPLAYABLE | LEDGER L-006/L-007; PLAN §Deferred |
