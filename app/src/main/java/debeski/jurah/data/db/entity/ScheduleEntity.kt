package debeski.jurah.data.db.entity

import androidx.room.ColumnInfo
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
    // A SQL default lets Room write the v1 -> v2 auto-migration for this column.
    @ColumnInfo(defaultValue = "0") val indexBase: Int = 0,
)
