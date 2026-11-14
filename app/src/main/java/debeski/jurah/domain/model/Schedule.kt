package debeski.jurah.domain.model

enum class ScheduleStatus { ACTIVE, PAUSED, COMPLETED }

/**
 * A medication course: a first dose at [startAtMillis], repeating every
 * [intervalMinutes], for [durationDays] days (null means ongoing/indefinite).
 *
 * Dose times are a deterministic grid derived from these three fields rather
 * than stored rows, so an indefinite course costs nothing to represent.
 *
 * [indexBase] is the dose index that lands on [startAtMillis]. It is 0 until the
 * course is re-timed: editing the start or the interval moves every later dose, so
 * the grid is re-anchored past the doses already recorded. Without that, a new
 * index 0 would overwrite the first dose the user actually took.
 */
data class Schedule(
    val id: Long = 0L,
    val name: String? = null,
    val startAtMillis: Long,
    val intervalMinutes: Int,
    val durationDays: Int? = null,
    val status: ScheduleStatus = ScheduleStatus.ACTIVE,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val indexBase: Int = 0,
)
