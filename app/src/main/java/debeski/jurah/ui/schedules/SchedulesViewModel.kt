package debeski.jurah.ui.schedules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import debeski.jurah.alarm.AlarmScheduler
import debeski.jurah.data.repo.DoseRepository
import debeski.jurah.data.repo.ScheduleRepository
import debeski.jurah.di.AppContainer
import debeski.jurah.domain.model.DoseEvent
import debeski.jurah.domain.model.DoseStatus
import debeski.jurah.domain.model.Schedule
import debeski.jurah.domain.model.ScheduleStatus
import debeski.jurah.domain.schedule.DoseCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ScheduleCard(
    val schedule: Schedule,
    val nextDoseAt: Long?,
    val nextDoseNumber: Int?,
    val totalDoses: Int?,
    val takenCount: Int,
    val missedCount: Int,
    val isActive: Boolean,
)

data class SchedulesUiState(
    val active: List<ScheduleCard> = emptyList(),
    val inactive: List<ScheduleCard> = emptyList(),
    val now: Long = System.currentTimeMillis(),
    val loaded: Boolean = false,
    val exactAlarmsAllowed: Boolean = true,
)

class SchedulesViewModel(
    private val schedules: ScheduleRepository,
    private val doses: DoseRepository,
    private val alarms: AlarmScheduler,
) : ViewModel() {

    // Cards show a live countdown, so the state re-derives on a slow tick.
    private val ticks = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(TICK_MILLIS)
        }
    }

    val state: StateFlow<SchedulesUiState> =
        combine(schedules.observeAll(), doses.observeAll(), ticks) { rows, events, now ->
            val eventsBySchedule = events.groupBy { it.scheduleId }
            val cards = rows.map { card(it, eventsBySchedule[it.id].orEmpty(), now) }
            SchedulesUiState(
                active = cards.filter { it.isActive },
                inactive = cards.filterNot { it.isActive },
                now = now,
                loaded = true,
                exactAlarmsAllowed = alarms.canScheduleExact(),
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SchedulesUiState())

    private fun card(schedule: Schedule, events: List<DoseEvent>, now: Long): ScheduleCard {
        val nextIndex = DoseCalculator.nextIndexAt(schedule, now)
        val actedIndices = events.map { it.doseIndex }.toSet()
        val elapsed = DoseCalculator.elapsedIndices(schedule, now)
        // A dose only counts as missed once its grace period has also passed, so the
        // card does not accuse the user the moment a reminder fires.
        val missed = elapsed.count { index ->
            index !in actedIndices &&
                DoseCalculator.scheduledAt(schedule, index) < now - GRACE_MILLIS
        }
        return ScheduleCard(
            schedule = schedule,
            nextDoseAt = nextIndex?.let { DoseCalculator.scheduledAt(schedule, it) },
            nextDoseNumber = nextIndex?.plus(1),
            totalDoses = DoseCalculator.totalDoses(schedule),
            takenCount = events.count { it.status == DoseStatus.TAKEN },
            missedCount = missed,
            isActive = schedule.status == ScheduleStatus.ACTIVE && nextIndex != null,
        )
    }

    fun pause(card: ScheduleCard) = viewModelScope.launch {
        val updated = card.schedule.copy(status = ScheduleStatus.PAUSED)
        schedules.update(updated)
        alarms.cancel(updated.id)
    }

    fun resume(card: ScheduleCard) = viewModelScope.launch {
        val updated = card.schedule.copy(status = ScheduleStatus.ACTIVE)
        schedules.update(updated)
        alarms.arm(updated)
    }

    fun delete(card: ScheduleCard) = viewModelScope.launch {
        alarms.cancel(card.schedule.id)
        schedules.delete(card.schedule)
    }

    /** Re-arms every active course; used after the exact-alarm grant changes. */
    fun reconcileAlarms() = viewModelScope.launch {
        alarms.armAll(schedules.active())
    }

    companion object {
        private const val TICK_MILLIS = 30_000L
        private const val GRACE_MILLIS = 60 * 60 * 1000L

        fun factory(container: AppContainer) = viewModelFactory {
            initializer {
                SchedulesViewModel(
                    schedules = container.scheduleRepository,
                    doses = container.doseRepository,
                    alarms = container.alarmScheduler,
                )
            }
        }
    }
}
