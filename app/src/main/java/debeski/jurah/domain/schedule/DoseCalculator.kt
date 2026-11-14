package debeski.jurah.domain.schedule

import debeski.jurah.domain.model.Schedule

/**
 * Dose times are a pure function of (startAt, interval, duration), never stored rows.
 * An indefinite course therefore costs nothing, and editing a schedule can never
 * leave orphaned dose records behind.
 */
object DoseCalculator {

    private const val MINUTE_MILLIS = 60_000L
    private const val MINUTES_PER_DAY = 24 * 60

    fun stepMillis(schedule: Schedule): Long = schedule.intervalMinutes * MINUTE_MILLIS

    /** Number of doses in the course, or null when it runs indefinitely. */
    fun totalDoses(schedule: Schedule): Int? = schedule.durationDays?.let { days ->
        val spanMinutes = days.toLong() * MINUTES_PER_DAY
        (spanMinutes / schedule.intervalMinutes).toInt().coerceAtLeast(1)
    }

    fun scheduledAt(schedule: Schedule, index: Int): Long =
        schedule.startAtMillis + index.toLong() * stepMillis(schedule)

    fun lastDoseAt(schedule: Schedule): Long? =
        totalDoses(schedule)?.let { scheduledAt(schedule, it - 1) }

    /** First dose due at or after [now]; null once the course is over. */
    fun nextIndexAt(schedule: Schedule, now: Long): Int? {
        val step = stepMillis(schedule)
        val delta = now - schedule.startAtMillis
        val index = if (delta <= 0L) 0 else ceilDiv(delta, step).toInt()
        val total = totalDoses(schedule)
        return if (total != null && index >= total) null else index
    }

    /**
     * Dose indices whose time has already arrived. Any index in here without a
     * recorded event was missed.
     */
    fun elapsedIndices(schedule: Schedule, now: Long): IntRange {
        val delta = now - schedule.startAtMillis
        if (delta < 0L) return IntRange.EMPTY
        val last = (delta / stepMillis(schedule)).toInt()
        val total = totalDoses(schedule)
        return 0..(if (total != null) minOf(last, total - 1) else last)
    }

    fun isFinished(schedule: Schedule, now: Long): Boolean = nextIndexAt(schedule, now) == null

    fun dosesPerDay(schedule: Schedule): Float =
        MINUTES_PER_DAY.toFloat() / schedule.intervalMinutes

    private fun ceilDiv(a: Long, b: Long): Long = (a + b - 1) / b
}
