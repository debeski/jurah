package debeski.jurah.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String?,
    val startAtMillis: Long,
    val intervalMinutes: Int,
    val durationDays: Int?,
    val status: String,
    val createdAtMillis: Long,
)
