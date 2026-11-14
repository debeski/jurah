# Build and release

## Toolchain

| Component | Version | Note |
|---|---|---|
| Gradle | 9.3.1 | wrapper |
| Android Gradle Plugin | 9.1.0 | |
| Kotlin | 2.4.0 | |
| KSP | 2.3.14 | versions independently of Kotlin now |
| Compose BOM | 2026.09.00 | |
| Room | 2.8.5 | |
| compileSdk / targetSdk / minSdk | 37 / 36 / 26 | |

`minSdk 26` gives `java.time` without desugaring and notification channels natively.

## The two Gradle flags

`gradle.properties` carries:

```properties
android.builtInKotlin=false
android.newDsl=false
```

Both are required, and both are transitional. AGP 9 compiles Kotlin itself by
default, and **KSP cannot attach to that built-in compilation** — the build fails
with "KSP is not compatible with Android Gradle Plugin's built-in Kotlin". Turning
it off hands compilation back to the Kotlin Gradle plugin, which in turn refuses
AGP 9's new DSL, hence the second flag.

AGP 10 removes both. By then KSP should support built-in Kotlin; if it does, drop
the flags and the `kotlin-android` plugin. The alternative today is pinning the
whole AndroidX dependency set backwards, since current AndroidX requires AGP 9.1+
and compileSdk 37.

## Commands

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleRelease
```

Room exports schemas to `app/schemas/`. Commit them — they are what makes a future
migration verifiable. The database is at version 2; v1 → v2 adds
`schedules.indexBase` and is an `@AutoMigration`, which Room can only generate
because the column declares a SQL `defaultValue`.

## Still to do before publishing

- **Release signing.** Generate an upload keystore, add a `signingConfig` fed from a
  gitignored `keystore.properties`, and switch the release build to it. Never commit
  the keystore.
- **Play Console.** A new personal developer account must run a closed test with
  at least 12 testers opted in for 14 continuous days before it can apply for
  production access. Start that clock early — it is usually the long pole.
- **Store listing.** Icon (done), feature graphic, screenshots per locale, short and
  full description in English and Arabic.
- **Data safety form.** Required even though the app collects nothing; declare no
  collection and no sharing. A privacy policy URL is needed for the listing.
- **Verify the release APK on a device**, not just that it builds: R8 can strip
  something Room or Compose reaches reflectively.
