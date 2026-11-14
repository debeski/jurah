# Jurah (جرعة) — Workspace Tracker

## Current Snapshot
Android medication-reminder app, Kotlin + Jetpack Compose, package `debeski.jurah`,
version 0.1.0 (untagged). Full loop works and is verified on an API 36 emulator:
create schedule → exact alarm fires → notification with Taken/Snooze → dose recorded →
history. Debug and minified release both build; release APK is 2.0 MB.

## Adopted Standards & Rules
- Gradle 9.3.1 / AGP 9.1.0 / Kotlin 2.4.0 / KSP 2.3.14 / Compose BOM 2026.09.00 / Room 2.8.5.
- compileSdk 37, targetSdk 36, minSdk 26.
- `android.builtInKotlin=false` + `android.newDsl=false` are REQUIRED (KSP cannot attach
  to AGP 9 built-in Kotlin). Both are removed at AGP 10 — see docs/build-and-release.md.
- Dose times are computed from (startAt, interval, durationDays), never stored as rows.
- One chained exact alarm per schedule; the receiver arms the next dose.
- `SCHEDULE_EXACT_ALARM`, not `USE_EXACT_ALARM` (Play policy risk).
- No dynamic colour: the magenta/blue scheme is brand identity.
- Own `ImageVector` icon set; Material icon artifacts are deprecated/frozen.
- No network permission. Keep it that way — the privacy claim in About depends on it.
- Name is Jurah / جرعة, package `debeski.jurah`. Avoid the bare "Jura" spelling: JURA
  Elektroapparate AG holds live Class 9 software marks. Package ID is permanent once published.

## Known Bugs & Unsolved Issues
- None open. Fixed during build: pill glyph drew as an infinity symbol (bad arc
  endpoints); selected FilterChip fell back to Material's blue `secondaryContainer`;
  stepper "decrease" used a ✕ instead of a minus.
- Release build took ~2h wall-clock under R8 while an emulator was running. Not
  investigated; re-time it on an idle machine before assuming it is normal.

## Active & Pending Tasks
- [ ] Decide whether to commit (nothing committed yet; `git init` done, no commits).
- [ ] Rename the checkout dir `~/Desktop/depy/app-pilly` → `app-jurah` (workspace rule §5);
      not done here because it would invalidate the running session's working directory.
- [ ] Release signing: upload keystore + gitignored `keystore.properties`.
- [ ] Play Console: closed test, 12 testers × 14 continuous days, before production.
- [ ] Store listing: feature graphic, per-locale screenshots, EN/AR descriptions.
- [ ] Data safety form + privacy policy URL.
- [ ] Edit an existing schedule (only create/pause/resume/delete exist today).
- [ ] Instrumented tests for the receivers; only `DoseCalculator` is unit-tested.
- [ ] Consider a "taken late" path — Taken always records against the fired dose index.

### Completed Recently
- Full app: schedules list, add flow, alarm engine, notifications, history, about.
- Bilingual EN/AR with correct Arabic plural categories; RTL mirroring verified.
- Cairo variable font bundled (OFL, attributed in About).
- Adaptive launcher icon incl. monochrome/themed variant.

## Validation & Docs Log
- Tests: `./gradlew :app:testDebugUnitTest` → 12/12 pass (DoseCalculator).
- Manual on emulator API 36: alarm fired exactly on time, chained next dose at +8h,
  Taken wrote `dose_events`, history rendered, Arabic RTL put the FAB bottom-left.
  Minified release installed and armed an alarm (Room survives R8).
- After the Jurah rename: build + 12/12 tests pass; on-device smoke test wrote to
  `jurah.db` and armed `debeski.jurah.action.DOSE_DUE`; Arabic app name جرعة renders.
- Docs: README.md, docs/architecture.md, docs/build-and-release.md, CHANGELOG.md v0.1.0.

## Global Helpers & Rules
- Emulator: `~/Library/Android/sdk/emulator/emulator -avd Vera_API36`; adb at
  `~/Library/Android/sdk/platform-tools/adb`. Stop the emulator when done.
- Inspect DB (debug builds only): `adb shell run-as debeski.jurah sqlite3 databases/jurah.db`.
- Check armed alarms: `adb shell dumpsys alarm | grep -A3 debeski.jurah.action.DOSE_DUE`.
- Force a dose soon: UPDATE `startAtMillis` via sqlite, then restart the app (it
  re-arms on resume).
- Per-app locale: `adb shell cmd locale set-app-locales debeski.jurah --locales ar`.

## Agent Handoff
Nothing is committed yet — the working tree holds the entire app. Ask before
committing. Next substantive work is release signing and the Play closed-test track;
next product gap is editing an existing schedule.
