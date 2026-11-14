package debeski.jurah.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import debeski.jurah.data.db.entity.DoseEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseEventDao {
    @Query("SELECT * FROM dose_events ORDER BY scheduledAtMillis DESC")
    fun observeAll(): Flow<List<DoseEventEntity>>

    @Query("SELECT * FROM dose_events WHERE scheduleId = :scheduleId ORDER BY doseIndex ASC")
    fun observeForSchedule(scheduleId: Long): Flow<List<DoseEventEntity>>

    @Query("SELECT * FROM dose_events WHERE scheduleId = :scheduleId ORDER BY doseIndex ASC")
    suspend fun forSchedule(scheduleId: Long): List<DoseEventEntity>

    @Query("SELECT COUNT(*) FROM dose_events WHERE scheduleId = :scheduleId")
    suspend fun countForSchedule(scheduleId: Long): Int

    @Query("SELECT MAX(doseIndex) FROM dose_events WHERE scheduleId = :scheduleId")
    suspend fun maxDoseIndex(scheduleId: Long): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(event: DoseEventEntity)

    @Query("DELETE FROM dose_events WHERE scheduleId = :scheduleId AND doseIndex = :doseIndex")
    suspend fun deleteSlot(scheduleId: Long, doseIndex: Int)
}
