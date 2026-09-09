# BRIEF — view-consumer-gaps

| | |
|---|---|
| Mode | LIBRARY |
| Owner | @teo |
| Opened | 2026-09-08 |
| Origin | `TODO.md` tasks T-1..T-5 — five view-layer gaps measured in mppArel's E2E workshop walkthrough (mppArel `blueprints/EjecucionTallerMoldes(MODULE)/LEDGER.md` L-104/L-106, G-022), parked 2026-09-08 by Owner decision, opened 2026-09-08 by Owner instruction ("start the fixes per your recommendation") |

## Why now

Each gap was measured against fsLib 6.2.4 + KVision 9.6.0 in a real consumer. The costliest
(T-3) multiplies every form-open by 7–9 identical RPCs per remote selector and pollutes the
native `<select>` with duplicate options — every consumer of `tomSelectRemote` pays it. T-1 and
T-4 are user-facing string defects that violate fsLib's own language policy (English sources +
KVision i18n). mppArel carries downstream workarounds for T-2 (`LAYOUTS_OBSOLETOS`) and T-5
(`AvisosDeTaller` bus) — library debt each future consumer would re-pay.

## Scope — first wave (implemented)

- **T-3** (`TomSelectRemoteInput.refreshState` has no in-flight guard — KVision
  `kvision-tom-select-remote` 9.6.0 lines 117-127): fsLib ships `FsTomSelectRemote` /
  `FsTomSelectRemoteInput` mirroring KVision's API with the guard, as an interim workaround with
  an upstream-retirement condition (the `FsTabPanel` precedent).
- **T-1** (`ViewItem.backCloseAction` cancel dialog hardcodes "Please Confirm" / "Cancel and
  forget current changes?" and inherits raw "Yes"/"No"): route all four through `I18n.tr`.
- **T-4** (`helpButtons` captures `viewLabel` when the FAB is built, so read-mode items show a
  stale/empty label; the file also hardcodes Spanish and English literals): late-bind the label
  via a provider overload; route the file's user-facing literals through `I18n.tr`.

## Scope — design decisions only (no implementation this wave)

- **T-2** (Tabulator layout persistence freezes `title` + column order): options and
  recommendation recorded in LEDGER L-006; implementation pending Owner decision.
- **T-5** (`openViewItem(modal)` has no close/write callback): semantics options recorded in
  LEDGER L-007; mppArel is unblocked (its L-106 app bus), lowest priority.

## Non-goals

- A full i18n sweep of all `fullstack` jsMain user-facing strings (only the two touched files
  this wave; the class-level sweep is a candidate follow-up).
- Changing `HelpType`/`HelpTheme` enum label values in `:core` (Spanish keys wrapped at point
  of use; relocation needs its own decision — see LEDGER L-005).
- Submitting the upstream KVision patch (recommended and tracked in L-003, not gating).
- T-2/T-5 implementation.

## Consumers

mppArel (fsLib 6.2.4, tracks head — measures OC-01's end-to-end target); ticketLib (pinned
3.2.1, unaffected until its major migration).
