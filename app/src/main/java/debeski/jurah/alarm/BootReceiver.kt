package debeski.jurah.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import debeski.jurah.JurahApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Alarms do not survive a reboot, an app update, or the moment the user finally
 * grants exact alarms — so every active course is re-armed on all three.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val container = (context.applicationContext as JurahApp).container
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                DoseNotifications.ensureChannel(context)
                container.alarmScheduler.armAll(container.scheduleRepository.active())
            } finally {
                pending.finish()
            }
        }
    }
}
