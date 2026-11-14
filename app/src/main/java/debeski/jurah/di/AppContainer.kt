package debeski.jurah.di

import android.content.Context
import debeski.jurah.alarm.AlarmScheduler
import debeski.jurah.data.db.JurahDatabase
import debeski.jurah.data.repo.DoseRepository
import debeski.jurah.data.repo.ScheduleRepository

/**
 * Hand-rolled DI. The graph is small enough that a container beats an annotation
 * processor here: no extra build step, no version coupling, and receivers can
 * reach it straight off the Application.
 */
class AppContainer(context: Context) {
    private val database: JurahDatabase by lazy { JurahDatabase.build(context) }

    val scheduleRepository: ScheduleRepository by lazy { ScheduleRepository(database.scheduleDao()) }
    val doseRepository: DoseRepository by lazy { DoseRepository(database.doseEventDao()) }
    val alarmScheduler: AlarmScheduler by lazy { AlarmScheduler(context) }
}
