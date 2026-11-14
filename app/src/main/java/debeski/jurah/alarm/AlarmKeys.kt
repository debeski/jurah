package debeski.jurah.alarm

/** Intent contract shared by the scheduler and its receivers. */
object AlarmKeys {
    const val ACTION_DOSE_DUE = "debeski.jurah.action.DOSE_DUE"
    const val ACTION_DOSE_TAKEN = "debeski.jurah.action.DOSE_TAKEN"
    const val ACTION_DOSE_SNOOZE = "debeski.jurah.action.DOSE_SNOOZE"

    const val EXTRA_SCHEDULE_ID = "scheduleId"
    const val EXTRA_DOSE_INDEX = "doseIndex"
    const val EXTRA_SCHEDULED_AT = "scheduledAt"
    const val EXTRA_IS_SNOOZE = "isSnooze"

    const val SNOOZE_MINUTES = 10
}
