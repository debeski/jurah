package debeski.jurah.data.repo

import debeski.jurah.data.db.DoseEventDao
import debeski.jurah.data.db.toDomain
import debeski.jurah.data.db.toEntity
import debeski.jurah.domain.model.DoseEvent
import debeski.jurah.domain.model.DoseStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DoseRepository(private val dao: DoseEventDao) {

    fun observeAll(): Flow<List<DoseEvent>> = dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    fun observeForSchedule(scheduleId: Long): Flow<List<DoseEvent>> =
        dao.observeForSchedule(scheduleId).map { rows -> rows.map { it.toDomain() } }

    suspend fun forSchedule(scheduleId: Long): List<DoseEvent> =
        dao.forSchedule(scheduleId).map { it.toDomain() }

    /** Highest dose index ever recorded for a course, or null when none has been. */
    suspend fun maxDoseIndex(scheduleId: Long): Int? = dao.maxDoseIndex(scheduleId)

    suspend fun record(
        scheduleId: Long,
        doseIndex: Int,
        scheduledAtMillis: Long,
        status: DoseStatus,
        actedAtMillis: Long = System.currentTimeMillis(),
    ) = dao.upsert(
        DoseEvent(
            scheduleId = scheduleId,
            doseIndex = doseIndex,
            scheduledAtMillis = scheduledAtMillis,
            actedAtMillis = actedAtMillis,
            status = status,
        ).toEntity()
    )

    suspend fun clearSlot(scheduleId: Long, doseIndex: Int) = dao.deleteSlot(scheduleId, doseIndex)
}
