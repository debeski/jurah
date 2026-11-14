package debeski.jurah.domain.schedule

import debeski.jurah.domain.model.Schedule

/**
 * Every dose time in a course, derived rather than stored.
 *
 * A dose index is absolute and never reused: index [Schedule.indexBase] is the one
 * that falls on [Schedule.startAtMillis], and anything below it belongs to the
 * course as it was timed before an edit. Dose numbers shown to the user are
 * therefore continuous across a re-timing.
 */
object DoseCalculator {
    private const val MINUTE_MILLIS = 60_000L
    private const val MINUTES_PER_DAY = 24 * 60

    fun stepMillis(schedule: Schedule): Long = schedule.intervalMinutes * MINUTE_MILLIS

    /** Doses the current grid holds: what [Schedule.durationDays] buys at this interval. */
    fun gridDoses(schedule: Schedule): Int? = schedule.durationDays?.let { days ->
        val spanMinutes = days.toLong() * MINUTES_PER_DAY
        (spanMinutes / schedule.intervalMinutes).toInt().coerceAtLeast(1)
    }

    /**
     * Doses in the whole course, counting any recorded before it was re-timed, so the
     * final dose is always index `totalDoses - 1`.
     */
    fun totalDoses(schedule: Schedule): Int? =
        gridDoses(schedule)?.let { schedule.indexBase + it }

    fun scheduledAt(schedule: Schedule, index: Int): Long =
        schedule.startAtMillis + (index - schedule.indexBase).toLong() * stepMillis(schedule)

    fun lastDoseAt(schedule: Schedule): Long? =
        totalDoses(schedule)?.let { scheduledAt(schedule, it - 1) }

    fun nextIndexAt(schedule: Schedule, now: Long): Int? {
        val step = stepMillis(schedule)
        val delta = now - schedule.startAtMillis
        val index = schedule.indexBase + if (delta <= 0L) 0 else ceilDiv(delta, step).toInt()
        val total = totalDoses(schedule)
        return if (total != null && index >= total) null else index
    }

    /** Slots whose time has arrived, bounded by the end of a finite course. */
    fun elapsedIndices(schedule: Schedule, now: Long): IntRange {
        val delta = now - schedule.startAtMillis
        if (delta < 0L) return IntRange.EMPTY
        val last = schedule.indexBase + (delta / stepMillis(schedule)).toInt()
        val total = totalDoses(schedule)
        return schedule.indexBase..(if (total != null) minOf(last, total - 1) else last)
    }

    fun isFinished(schedule: Schedule, now: Long): Boolean = nextIndexAt(schedule, now) == null

    fun dosesPerDay(schedule: Schedule): Float = MINUTES_PER_DAY.toFloat() / schedule.intervalMinutes

    private fun ceilDiv(a: Long, b: Long): Long = (a + b - 1) / b
}
