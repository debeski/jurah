package debeski.jurah.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.text.format.DateFormat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import debeski.jurah.MainActivity
import debeski.jurah.R
import debeski.jurah.domain.model.Schedule
import debeski.jurah.domain.schedule.DoseCalculator
import java.util.Date

object DoseNotifications {

    const val CHANNEL_ID = "dose_reminders"

    fun ensureChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.channel_dose_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.channel_dose_description)
            enableVibration(true)
            vibrationPattern = longArrayOf(0L, 350L, 250L, 350L)
            setShowBadge(true)
            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                    .build(),
            )
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /** One notification per schedule, so a new dose replaces that medication's last reminder. */
    fun notifyDose(context: Context, schedule: Schedule, doseIndex: Int, scheduledAt: Long) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        val total = DoseCalculator.totalDoses(schedule)
        val clock = DateFormat.getTimeFormat(context).format(Date(scheduledAt))
        val title = schedule.name?.takeIf { it.isNotBlank() }
            ?: context.getString(R.string.notification_title_fallback)
        val body = if (total != null) {
            context.getString(R.string.notification_body_counted, doseIndex + 1, total, clock)
        } else {
            context.getString(R.string.notification_body_ongoing, clock)
        }

        val open = PendingIntent.getActivity(
            context,
            schedule.id.toInt(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setWhen(scheduledAt)
            .setShowWhen(true)
            .setAutoCancel(true)
            .setContentIntent(open)
            .addAction(
                R.drawable.ic_notification_check,
                context.getString(R.string.action_mark_taken),
                action(context, AlarmKeys.ACTION_DOSE_TAKEN, schedule.id, doseIndex, scheduledAt),
            )
            .addAction(
                R.drawable.ic_notification_snooze,
                context.getString(R.string.action_snooze),
                action(context, AlarmKeys.ACTION_DOSE_SNOOZE, schedule.id, doseIndex, scheduledAt),
            )
            .build()

        manager.notify(schedule.id.toInt(), notification)
    }

    fun cancel(context: Context, scheduleId: Long) =
        NotificationManagerCompat.from(context).cancel(scheduleId.toInt())

    private fun action(
        context: Context,
        actionName: String,
        scheduleId: Long,
        doseIndex: Int,
        scheduledAt: Long,
    ): PendingIntent = PendingIntent.getBroadcast(
        context,
        (actionName.hashCode() xor scheduleId.toInt() xor doseIndex),
        Intent(context, DoseActionReceiver::class.java).apply {
            action = actionName
            data = Uri.parse("jurah://action/$scheduleId/$doseIndex")
            putExtra(AlarmKeys.EXTRA_SCHEDULE_ID, scheduleId)
            putExtra(AlarmKeys.EXTRA_DOSE_INDEX, doseIndex)
            putExtra(AlarmKeys.EXTRA_SCHEDULED_AT, scheduledAt)
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}
