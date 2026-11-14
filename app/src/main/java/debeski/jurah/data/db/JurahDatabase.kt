package debeski.jurah.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import debeski.jurah.data.db.entity.DoseEventEntity
import debeski.jurah.data.db.entity.ScheduleEntity

@Database(
    entities = [ScheduleEntity::class, DoseEventEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class JurahDatabase : RoomDatabase() {
    abstract fun scheduleDao(): ScheduleDao
    abstract fun doseEventDao(): DoseEventDao

    companion object {
        fun build(context: Context): JurahDatabase =
            Room.databaseBuilder(context, JurahDatabase::class.java, "jurah.db")
                .build()
    }
}
