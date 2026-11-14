package debeski.jurah.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import debeski.jurah.domain.model.Schedule
import debeski.jurah.domain.model.ScheduleStatus
import debeski.jurah.domain.schedule.DoseCalculator

/**
 * Arms exactly one alarm per schedule — the next dose — and each firing arms the
 * one after it.
 *
 * That chaining matters: the OS caps how many exact alarms an app may hold, and a
 * course can run for months. One pending alarm per schedule also means editing or
 * stopping a course can never leave a stale alarm armed.
 */
class AlarmScheduler(private val context: Context) {

    private val alarmManager: AlarmManager = context.getSystemService(AlarmManager::class.java)

    /**
     * Android 12 gates exact alarms behind a user grant. Without it, dose times drift
     * with Doze, so the UI surfaces this rather than failing quietly.
     */
    fun canScheduleExact(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    /** Arms the next dose due from now. */
    fun arm(schedule: Schedule) {
        cancel(schedule.id)
        if (schedule.status != ScheduleStatus.ACTIVE) return
        val index = DoseCalculator.nextIndexAt(schedule, System.currentTimeMillis()) ?: return
        armIndex(schedule, index)
    }

    /** Arms one specific dose. Returns false when [index] is past the end of the course. */
    fun armIndex(schedule: Schedule, index: Int): Boolean {
        val total = DoseCalculator.totalDoses(schedule)
        if (total != null && index >= total) return false
        val at = DoseCalculator.scheduledAt(schedule, index)
        val intent = PendingIntent.getBroadcast(
            context,
            requestCode(schedule.id),
            dueIntent(schedule.id).apply {
                putExtra(AlarmKeys.EXTRA_SCHEDULE_ID, schedule.id)
                putExtra(AlarmKeys.EXTRA_DOSE_INDEX, index)
                putExtra(AlarmKeys.EXTRA_SCHEDULED_AT, at)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        if (canScheduleExact()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, intent)
        } else {
            // Best effort until the user grants exact alarms: a window keeps it near the hour.
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, at, WINDOW_MILLIS, intent)
        }
        return true
    }

    fun armAll(schedules: List<Schedule>) = schedules.forEach(::arm)

    fun cancel(scheduleId: Long) {
        // PendingIntent equality ignores extras, so an empty-extra twin cancels the armed one.
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                requestCode(scheduleId),
                dueIntent(scheduleId),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        )
    }

    /** Re-raises the same dose after a delay, without disturbing the course's own chain. */
    fun snooze(scheduleId: Long, doseIndex: Int, scheduledAt: Long, minutes: Int = AlarmKeys.SNOOZE_MINUTES) {
        val at = System.currentTimeMillis() + minutes * 60_000L
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmKeys.ACTION_DOSE_DUE
            data = Uri.parse("jurah://snooze/$scheduleId/$doseIndex")
            putExtra(AlarmKeys.EXTRA_SCHEDULE_ID, scheduleId)
            putExtra(AlarmKeys.EXTRA_DOSE_INDEX, doseIndex)
            putExtra(AlarmKeys.EXTRA_SCHEDULED_AT, scheduledAt)
            putExtra(AlarmKeys.EXTRA_IS_SNOOZE, true)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            snoozeRequestCode(scheduleId, doseIndex),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        if (canScheduleExact()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending)
        } else {
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, at, WINDOW_MILLIS, pending)
        }
    }

    private fun dueIntent(scheduleId: Long) = Intent(context, AlarmReceiver::class.java).apply {
        action = AlarmKeys.ACTION_DOSE_DUE
        data = Uri.parse("jurah://dose/$scheduleId")
    }

    private fun requestCode(scheduleId: Long) = scheduleId.toInt()

    private fun snoozeRequestCode(scheduleId: Long, doseIndex: Int) =
        (scheduleId.toInt() * 31 + doseIndex) or SNOOZE_BIT

    private companion object {
        const val WINDOW_MILLIS = 10 * 60 * 1000L
        const val SNOOZE_BIT = 1 shl 30
    }
}
