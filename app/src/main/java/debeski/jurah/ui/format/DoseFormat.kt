package debeski.jurah.ui.format

import android.content.Context
import android.text.format.DateFormat
import debeski.jurah.R
import debeski.jurah.domain.schedule.IntervalPreset
import java.time.Instant
import java.time.ZoneId
import java.util.Date
import java.util.concurrent.TimeUnit

/** All user-facing time text, routed through the user's own 12/24-hour and locale settings. */
object DoseFormat {

    fun time(context: Context, millis: Long): String =
        DateFormat.getTimeFormat(context).format(Date(millis))

    fun date(context: Context, millis: Long): String =
        DateFormat.getMediumDateFormat(context).format(Date(millis))

    fun dateTime(context: Context, millis: Long): String =
        context.getString(R.string.date_time_pair, date(context, millis), time(context, millis))

    fun interval(context: Context, minutes: Int): String =
        when (IntervalPreset.forMinutes(minutes)) {
            IntervalPreset.FOUR_HOURLY -> context.getString(R.string.interval_4h)
            IntervalPreset.SIX_HOURLY -> context.getString(R.string.interval_6h)
            IntervalPreset.EIGHT_HOURLY -> context.getString(R.string.interval_8h)
            IntervalPreset.TWELVE_HOURLY -> context.getString(R.string.interval_12h)
            IntervalPreset.DAILY -> context.getString(R.string.interval_24h)
            null -> context.getString(R.string.interval_custom_hours, minutes / 60)
        }

    fun days(context: Context, days: Int): String =
        context.resources.getQuantityString(R.plurals.days_count, days, days)

    fun doses(context: Context, count: Int): String =
        context.resources.getQuantityString(R.plurals.doses_count, count, count)

    /** Local midnight for the day containing [millis]; used to bucket history. */
    fun startOfDay(millis: Long): Long {
        val zone = ZoneId.systemDefault()
        return Instant.ofEpochMilli(millis).atZone(zone).toLocalDate().atStartOfDay(zone)
            .toInstant().toEpochMilli()
    }

    /** "in 3h 12m" / "in 12m" / "now" — a countdown, not a clock reading. */
    fun countdown(context: Context, fromMillis: Long, toMillis: Long): String {
        val remaining = toMillis - fromMillis
        if (remaining <= TimeUnit.MINUTES.toMillis(1)) return context.getString(R.string.dose_due_now)
        val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(remaining)
        val days = totalMinutes / (24 * 60)
        val hours = (totalMinutes % (24 * 60)) / 60
        val minutes = totalMinutes % 60
        val span = when {
            days > 0 -> context.getString(R.string.span_days_hours, days, hours)
            hours > 0 -> context.getString(R.string.span_hours_minutes, hours, minutes)
            else -> context.getString(R.string.span_minutes, minutes)
        }
        return context.getString(R.string.dose_due_in, span)
    }
}
