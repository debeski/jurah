package debeski.jurah.domain.model

enum class DoseStatus { TAKEN, SKIPPED }

/** A dose the user actually acted on. Absence of an event for a past slot means "missed". */
data class DoseEvent(
    val id: Long = 0L,
    val scheduleId: Long,
    val doseIndex: Int,
    val scheduledAtMillis: Long,
    val actedAtMillis: Long,
    val status: DoseStatus,
)
