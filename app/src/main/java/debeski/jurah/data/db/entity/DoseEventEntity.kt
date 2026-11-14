package debeski.jurah.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dose_events",
    foreignKeys = [
        ForeignKey(
            entity = ScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index(value = ["scheduleId", "doseIndex"], unique = true)],
)
data class DoseEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val scheduleId: Long,
    val doseIndex: Int,
    val scheduledAtMillis: Long,
    val actedAtMillis: Long,
    val status: String,
)
