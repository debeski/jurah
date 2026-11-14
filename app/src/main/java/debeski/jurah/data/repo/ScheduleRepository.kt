package debeski.jurah.data.repo

import debeski.jurah.data.db.ScheduleDao
import debeski.jurah.data.db.toDomain
import debeski.jurah.data.db.toEntity
import debeski.jurah.domain.model.Schedule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ScheduleRepository(private val dao: ScheduleDao) {

    fun observeAll(): Flow<List<Schedule>> = dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    fun observe(id: Long): Flow<Schedule?> = dao.observeById(id).map { it?.toDomain() }

    suspend fun byId(id: Long): Schedule? = dao.byId(id)?.toDomain()

    suspend fun active(): List<Schedule> = dao.active().map { it.toDomain() }

    suspend fun create(schedule: Schedule): Long = dao.insert(schedule.toEntity())

    suspend fun update(schedule: Schedule) = dao.update(schedule.toEntity())

    suspend fun delete(schedule: Schedule) = dao.delete(schedule.toEntity())
}
