# Changelog

## v0.1.0

First working version: a complete schedule → reminder → history loop, verified on an
API 36 emulator.

- **Name and package**: ships as **Jurah** — Arabic **جرعة**, "a dose" — with package
  `debeski.jurah`. Picked over "Pilly" because `app.pilly` is already live on Play as
  another pill reminder, and over the bare "Jura" spelling because JURA Elektroapparate AG
  holds live word marks reaching into Class 9 software, which invites a Play trademark
  complaint. "Jurah" is also the truer romanisation of جرعة (the ة renders "ah").
- **Project foundation**: Kotlin 2.4.0 / Compose (BOM 2026.09.00) / AGP 9.1.0 on Gradle
  9.3.1, `compileSdk 37`, `minSdk 26`, `targetSdk 36`. Room 2.8.5 via KSP 2.3.14, with
  `android.builtInKotlin=false` and `android.newDsl=false` because KSP cannot attach to
  AGP 9's built-in Kotlin compilation.
- **Scheduling model** (`DoseCalculator`): dose times are a pure function of
  (`startAtMillis`, `intervalMinutes`, `durationDays`) rather than stored rows, so an
  indefinite course costs nothing and editing a schedule orphans no dose records.
  Covered by 12 unit tests.
- **Alarm engine** (`AlarmScheduler`, `AlarmReceiver`, `BootReceiver`): arms exactly one
  exact alarm per schedule and chains the next on each firing. Falls back to
  `setWindow` when the `SCHEDULE_EXACT_ALARM` grant is absent, and re-arms every active
  course on `BOOT_COMPLETED`, `MY_PACKAGE_REPLACED` and the exact-alarm permission change.
- **Dose reminders** (`DoseNotifications`): high-importance notification on the
  `dose_reminders` channel with Taken and Snooze actions; Taken records a `DoseEvent`,
  Snooze re-raises the same dose in 10 minutes without advancing the chain.
- **Schedules screen**: glass cards grouped Active/Inactive with a live countdown,
  dose progress, missed count (after a one-hour grace), and pause/resume/delete.
  In-context notices for the notification and exact-alarm permissions.
- **Add schedule**: optional name, calendar + clock pickers for the first dose,
  interval presets (4/6/8/12/24 h) plus custom hours, a day count or an ongoing toggle,
  and a live summary of total doses and end date.
- **History**: taken, skipped and missed doses over a 90-day window, grouped by day.
  Missed doses are derived from elapsed slots with no recorded event.
- **Theme**: magenta/blue Material 3 scheme (dynamic colour deliberately off), a
  glassmorphism surface treatment over a gradient backdrop, and the Cairo variable font
  instantiated per weight so Arabic letterforms stay correct when bold.
- **Icon set** (`JurahIcons`): the app draws its own glyphs as `ImageVector`s. Material's
  icon artifacts are deprecated and frozen, and none of them has a pill.
- **Font licensing**: Cairo's SIL OFL 1.1 text ships as `res/raw/ofl_cairo.txt` and is
  readable in-app via About → Typeface → View license. The OFL requires its text and
  copyright notice to travel with the font, which attribution alone does not satisfy.
- **RTL fix**: `JurahIcons` now supports `autoMirror`, set on the back chevron, which
  previously pointed left under an Arabic layout.
- **RTL**: the FAB sits bottom-right in English and bottom-left in Arabic via the
  Scaffold's end anchoring; all strings localised in `values-ar`, with correct Arabic
  plural categories for days and doses.
