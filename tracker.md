# Jurah (جرعة) — Workspace Tracker

## Current Snapshot
Repo: https://github.com/debeski/jurah (PUBLIC), branch `main`.
Android medication-reminder app, Kotlin + Jetpack Compose, package `debeski.jurah`,
version 0.1.0 (untagged). Full loop works and is verified on an API 36 emulator:
create or edit a schedule → exact alarm fires → notification with Taken/Snooze → dose
recorded → history. Debug and minified release both build; release APK is 2.0 MB.
Room database is at version 2 (`schedules.indexBase`), migrated via `@AutoMigration`.

## Adopted Standards & Rules
- Gradle 9.3.1 / AGP 9.1.0 / Kotlin 2.4.0 / KSP 2.3.14 / Compose BOM 2026.09.00 / Room 2.8.5.
- compileSdk 37, targetSdk 36, minSdk 26.
- `android.builtInKotlin=false` + `android.newDsl=false` are REQUIRED (KSP cannot attach
  to AGP 9 built-in Kotlin). Both are removed at AGP 10 — see docs/build-and-release.md.
- Dose times are computed from (startAt, interval, durationDays), never stored as rows.
- Re-timing a course moves `Schedule.indexBase` past the highest recorded dose index,
  so a new dose index can never overwrite a stored dose. Dose numbers stay continuous
  and `totalDoses` = indexBase + gridDoses. See docs/architecture.md.
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
- Backdating a start time reports the elapsed slots as missed. Same at create and at
  edit, so it is consistent, but it does accuse the user for a schedule that did not
  exist yet. Needs a product decision, not a bug fix.

## Active & Pending Tasks
- [ ] Enumerate Apache-2.0 AndroidX deps on the licences screen (only Cairo is listed).
- [ ] Polish: content scrolls under the transparent top bar; consider a scroll-aware scrim.
- [ ] Rename the checkout dir `~/Desktop/depy/app-pilly` → `app-jurah` (workspace rule §5);
      not done here because it would invalidate the running session's working directory.
- [ ] Release signing: upload keystore + gitignored `keystore.properties`.
- [ ] Play Console: closed test, 12 testers × 14 continuous days, before production.
- [ ] Store listing: feature graphic, per-locale screenshots, EN/AR descriptions.
- [ ] Data safety form + privacy policy URL.
- [ ] Instrumented tests for the receivers and the v1→v2 migration; only
      `DoseCalculator` is unit-tested.
- [ ] Consider a "taken late" path — Taken always records against the fired dose index.

### Completed Recently
- Edit an existing course: `ui/add` became `ui/editor`, one screen for create and edit,
  with grid re-anchoring so a re-timing keeps recorded doses.
- Cairo OFL 1.1 text bundled as a raw resource with an in-app licences screen.
- RTL fix: back chevron now auto-mirrors (it pointed the wrong way in Arabic).
- Full app: schedules list, add flow, alarm engine, notifications, history, about.
- Bilingual EN/AR with correct Arabic plural categories; RTL mirroring verified.
- Cairo variable font bundled (OFL, attributed in About).
- Adaptive launcher icon incl. monochrome/themed variant.

## Validation & Docs Log
- Tests: `./gradlew :app:testDebugUnitTest` → 18/18 pass (DoseCalculator).
- Edit flow on emulator API 36 (2026-11-15): installed over v1 data, Room migrated to
  v2 with no error; editor prefilled; changing the time showed the re-timing notice;
  after save `indexBase=2`, both recorded dose rows intact at their original times,
  one exact alarm armed at the new dose 4 time, card read "2 of 23 taken", history
  kept both doses. Arabic mirrored and back discarded an unsaved edit.
- Manual on emulator API 36: alarm fired exactly on time, chained next dose at +8h,
  Taken wrote `dose_events`, history rendered, Arabic RTL put the FAB bottom-left.
  Minified release installed and armed an alarm (Room survives R8).
- After the Jurah rename: build + 12/12 tests pass; on-device smoke test wrote to
  `jurah.db` and armed `debeski.jurah.action.DOSE_DUE`; Arabic app name جرعة renders.
- OFL screen verified on device in both locales; licence packaged as res/raw/ofl_cairo.txt.
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
v0.1.0 is committed and pushed to github.com/debeski/jurah (`main`, public), untagged.
Next substantive work is release signing and the Play closed-test track. The checkout
dir is still `app-pilly`.
