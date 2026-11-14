package debeski.jurah.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import debeski.jurah.data.db.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedules ORDER BY createdAtMillis DESC")
    fun observeAll(): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE id = :id")
    fun observeById(id: Long): Flow<ScheduleEntity?>

    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun byId(id: Long): ScheduleEntity?

    @Query("SELECT * FROM schedules WHERE status = 'ACTIVE'")
    suspend fun active(): List<ScheduleEntity>

    @Insert
    suspend fun insert(schedule: ScheduleEntity): Long

    @Update
    suspend fun update(schedule: ScheduleEntity)

    @Delete
    suspend fun delete(schedule: ScheduleEntity)
}
