# Architecture

Single Gradle module (`:app`), layered by package. Dependencies point inward:
`ui` → `domain` ← `data`, with `alarm` sitting beside them and reached through
`di/AppContainer`.

```
debeski.jurah
├── domain/       models + DoseCalculator (pure Kotlin, unit-tested)
├── data/         Room entities, DAOs, mappers, repositories
├── alarm/        AlarmScheduler, receivers, notification building
├── ui/           Compose screens, view models, theme, icons, formatting
└── di/           AppContainer (hand-rolled; no annotation processor)
```

## Dose times are computed, not stored

A schedule stores only `startAtMillis`, `intervalMinutes` and `durationDays`. Every
dose time follows from them:

```
scheduledAt(n) = startAtMillis + n * intervalMinutes * 60_000
totalDoses     = durationDays * 1440 / intervalMinutes     (null when ongoing)
```

This matters for three reasons:

- An indefinite course (diabetes, blood pressure) needs no rows at all.
- Editing a schedule cannot orphan pre-generated dose rows.
- "Missed" becomes derivable: an elapsed slot with no recorded event, past a
  one-hour grace period.

Only doses the user *acted on* are stored, in `dose_events`, keyed uniquely by
(`scheduleId`, `doseIndex`).

## One alarm per schedule, chained

`AlarmScheduler.arm()` sets a single exact alarm for the next due dose.
`AlarmReceiver` raises the notification and calls `armIndex(schedule, index + 1)`,
so the chain advances one dose at a time.

This is deliberate: the OS caps how many exact alarms an app may hold, and a
six-month course would otherwise want hundreds. Holding one pending alarm per
schedule also means editing, pausing or deleting a course can never leave a stale
alarm armed — `cancel()` builds an equal `PendingIntent` (equality ignores extras,
which is why the schedule id lives in the intent's `data` URI).

When the receiver runs past the final dose, `armIndex` returns false and the
schedule is marked `COMPLETED`.

## Degradation and recovery

| Situation | Behaviour |
|---|---|
| `SCHEDULE_EXACT_ALARM` not granted | Falls back to `setWindow` with a 10-minute window; the list screen shows a notice offering the grant |
| Notifications disabled | `notifyDose` returns early; the list screen offers the runtime request |
| Device rebooted | `BootReceiver` re-arms every active course |
| App updated | Same receiver, via `MY_PACKAGE_REPLACED` |
| Exact-alarm grant changes | Same receiver, via the permission-state broadcast; the UI also re-arms on resume |

Receivers do their database work inside `goAsync()` so the broadcast stays alive
for the coroutine.

## Why `SCHEDULE_EXACT_ALARM` and not `USE_EXACT_ALARM`

`USE_EXACT_ALARM` needs no user grant, but Play restricts it to apps whose core
function is an alarm clock or calendar. A medication reminder is not clearly inside
that policy, and being wrong means a rejected release. `SCHEDULE_EXACT_ALARM` is
user-granted, which costs an onboarding step but carries no policy risk.
