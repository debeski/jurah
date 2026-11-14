package debeski.jurah.domain.model

enum class ScheduleStatus { ACTIVE, PAUSED, COMPLETED }

/**
 * A medication course: a first dose at [startAtMillis], repeating every
 * [intervalMinutes], for [durationDays] days (null means ongoing/indefinite).
 *
 * Dose times are a deterministic grid derived from these three fields rather
 * than stored rows, so an indefinite course costs nothing to represent.
 */
data class Schedule(
    val id: Long = 0L,
    val name: String? = null,
    val startAtMillis: Long,
    val intervalMinutes: Int,
    val durationDays: Int? = null,
    val status: ScheduleStatus = ScheduleStatus.ACTIVE,
    val createdAtMillis: Long = System.currentTimeMillis(),
)
