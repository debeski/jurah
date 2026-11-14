package debeski.jurah.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import debeski.jurah.JurahApp
import debeski.jurah.domain.model.ScheduleStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Raises the dose reminder, then arms the following dose so the chain continues. */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getLongExtra(AlarmKeys.EXTRA_SCHEDULE_ID, -1L)
        if (scheduleId <= 0L) return
        val doseIndex = intent.getIntExtra(AlarmKeys.EXTRA_DOSE_INDEX, 0)
        val scheduledAt = intent.getLongExtra(AlarmKeys.EXTRA_SCHEDULED_AT, System.currentTimeMillis())
        val isSnooze = intent.getBooleanExtra(AlarmKeys.EXTRA_IS_SNOOZE, false)

        val pending = goAsync()
        val container = (context.applicationContext as JurahApp).container
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val schedule = container.scheduleRepository.byId(scheduleId) ?: return@launch
                if (schedule.status != ScheduleStatus.ACTIVE) return@launch

                DoseNotifications.notifyDose(context, schedule, doseIndex, scheduledAt)

                // A snoozed reminder is a repeat of a dose already in the chain, so it
                // must not advance the chain a second time.
                if (isSnooze) return@launch

                val armed = container.alarmScheduler.armIndex(schedule, doseIndex + 1)
                if (!armed) {
                    container.scheduleRepository.update(
                        schedule.copy(status = ScheduleStatus.COMPLETED)
                    )
                }
            } finally {
                pending.finish()
            }
        }
    }
}
