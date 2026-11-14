package debeski.jurah.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import debeski.jurah.alarm.AlarmScheduler
import debeski.jurah.data.repo.ScheduleRepository
import debeski.jurah.di.AppContainer
import debeski.jurah.domain.model.Schedule
import kotlinx.coroutines.launch

class AddScheduleViewModel(
    private val schedules: ScheduleRepository,
    private val alarms: AlarmScheduler,
) : ViewModel() {

    /** Saves the course and immediately arms its first due dose. */
    fun create(
        name: String?,
        startAtMillis: Long,
        intervalMinutes: Int,
        durationDays: Int?,
        onSaved: () -> Unit,
    ) = viewModelScope.launch {
        val id = schedules.create(
            Schedule(
                name = name?.trim()?.takeIf { it.isNotEmpty() },
                startAtMillis = startAtMillis,
                intervalMinutes = intervalMinutes,
                durationDays = durationDays,
            )
        )
        schedules.byId(id)?.let { alarms.arm(it) }
        onSaved()
    }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer {
                AddScheduleViewModel(container.scheduleRepository, container.alarmScheduler)
            }
        }
    }
}
