# Jurah

An advanced stopwatch for medication. You tell it when the first dose is, how often
it repeats and for how long; it reminds you at every dose and keeps a record of what
you took and what you missed. A course can be renamed, paused, re-timed or stopped
while it runs, and re-timing one keeps the doses it has already recorded.

Built for Android, in Kotlin and Jetpack Compose. English and Arabic, fully mirrored.

## Why native

The hard part of this app is not the UI, it is making an alarm fire at 03:00 on a
phone that is asleep. That means `AlarmManager.setExactAndAllowWhileIdle`, the
`SCHEDULE_EXACT_ALARM` grant Android 12 introduced, a boot receiver to re-arm after a
restart, and Doze handling. Those are platform APIs, used directly, with no plugin
layer in between.

## Build

Needs JDK 17+ and the Android SDK (compileSdk 37 installed).

```bash
./gradlew :app:assembleDebug        # debug APK
./gradlew :app:testDebugUnitTest    # unit tests
./gradlew :app:assembleRelease      # minified release APK (unsigned)
```

See [docs/build-and-release.md](docs/build-and-release.md) for the toolchain, the two
required Gradle flags and the route to Play, and [docs/architecture.md](docs/architecture.md)
for how scheduling works.

## Third-party

Cairo, by The Cairo Project Authors, under the [SIL Open Font License 1.1](app/src/main/res/raw/ofl_cairo.txt).
The licence text ships with the app and is readable under About → Typeface → View license.

Everything else is AndroidX/Jetpack under Apache 2.0; those are not yet enumerated in-app.

## Privacy

No network permission, no account, no analytics. All data is in a local Room database.
