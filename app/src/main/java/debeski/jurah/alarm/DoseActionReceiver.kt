package debeski.jurah.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import debeski.jurah.JurahApp
import debeski.jurah.domain.model.DoseStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Handles the Taken and Snooze buttons on a dose reminder. */
class DoseActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getLongExtra(AlarmKeys.EXTRA_SCHEDULE_ID, -1L)
        if (scheduleId <= 0L) return
        val doseIndex = intent.getIntExtra(AlarmKeys.EXTRA_DOSE_INDEX, 0)
        val scheduledAt = intent.getLongExtra(AlarmKeys.EXTRA_SCHEDULED_AT, System.currentTimeMillis())

        val pending = goAsync()
        val container = (context.applicationContext as JurahApp).container
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    AlarmKeys.ACTION_DOSE_TAKEN -> {
                        container.doseRepository.record(
                            scheduleId = scheduleId,
                            doseIndex = doseIndex,
                            scheduledAtMillis = scheduledAt,
                            status = DoseStatus.TAKEN,
                        )
                        DoseNotifications.cancel(context, scheduleId)
                    }

                    AlarmKeys.ACTION_DOSE_SNOOZE -> {
                        container.alarmScheduler.snooze(scheduleId, doseIndex, scheduledAt)
                        DoseNotifications.cancel(context, scheduleId)
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }
}
