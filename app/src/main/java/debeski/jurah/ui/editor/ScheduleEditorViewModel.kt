package debeski.jurah.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import debeski.jurah.alarm.AlarmScheduler
import debeski.jurah.data.repo.DoseRepository
import debeski.jurah.data.repo.ScheduleRepository
import debeski.jurah.di.AppContainer
import debeski.jurah.domain.model.Schedule
import debeski.jurah.domain.model.ScheduleStatus
import debeski.jurah.domain.schedule.DoseCalculator
import debeski.jurah.domain.schedule.IntervalPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/** The editable shape of a course, flattened into what the pickers actually bind to. */
data class ScheduleForm(
    val name: String,
    val epochDay: Long,
    val hour: Int,
    val minute: Int,
    val intervalMinutes: Int,
    val durationDays: Int?,
) {
    val ongoing: Boolean get() = durationDays == null

    fun startAtMillis(): Long = LocalDate.ofEpochDay(epochDay)
        .atTime(hour, minute)
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}

sealed interface EditorUiState {
    /** Reading the course being edited; a new course never passes through this. */
    data object Loading : EditorUiState

    /** The course was deleted from under the editor, so there is nothing left to edit. */
    data object Missing : EditorUiState

    data class Ready(
        val form: ScheduleForm,
        val editing: Boolean,
        val recordedDoses: Int,
    ) : EditorUiState
}

/**
 * Backs both creating and editing a course. Editing is the delicate one: the dose
 * grid is derived from the start time and the interval, so changing either moves
 * every dose and would let a new dose index collide with one already recorded.
 * Saving therefore re-anchors the grid past the highest recorded index.
 */
class ScheduleEditorViewModel(
    private val scheduleId: Long?,
    private val schedules: ScheduleRepository,
    private val doses: DoseRepository,
    private val alarms: AlarmScheduler,
) : ViewModel() {

    private val _state = MutableStateFlow<EditorUiState>(
        if (scheduleId == null) EditorUiState.Ready(blankForm(), editing = false, recordedDoses = 0)
        else EditorUiState.Loading
    )
    val state: StateFlow<EditorUiState> = _state.asStateFlow()

    private var original: Schedule? = null

    init {
        if (scheduleId != null) load(scheduleId)
    }

    private fun load(id: Long) = viewModelScope.launch {
        val schedule = schedules.byId(id)
        if (schedule == null) {
            _state.value = EditorUiState.Missing
            return@launch
        }
        original = schedule
        _state.value = EditorUiState.Ready(
            form = schedule.toForm(),
            editing = true,
            recordedDoses = doses.forSchedule(id).size,
        )
    }

    fun save(form: ScheduleForm, onSaved: () -> Unit) = viewModelScope.launch {
        val existing = original
        if (existing == null) create(form) else update(existing, form)
        onSaved()
    }

    private suspend fun create(form: ScheduleForm) {
        val id = schedules.create(
            Schedule(
                name = form.name.trim().takeIf { it.isNotEmpty() },
                startAtMillis = form.startAtMillis(),
                intervalMinutes = form.intervalMinutes,
                durationDays = form.durationDays,
            )
        )
        schedules.byId(id)?.let { alarms.arm(it) }
    }

    private suspend fun update(existing: Schedule, form: ScheduleForm) {
        val startAt = form.startAtMillis()
        val retimed = startAt != existing.startAtMillis ||
            form.intervalMinutes != existing.intervalMinutes

        // Only a re-timing invalidates the grid. A rename or a longer course leaves
        // every dose where it was, so the indices already recorded stay valid.
        val indexBase = if (retimed) {
            maxOf(existing.indexBase, (doses.maxDoseIndex(existing.id) ?: -1) + 1)
        } else {
            existing.indexBase
        }

        val draft = existing.copy(
            name = form.name.trim().takeIf { it.isNotEmpty() },
            startAtMillis = startAt,
            intervalMinutes = form.intervalMinutes,
            durationDays = form.durationDays,
            indexBase = indexBase,
        )

        // A course that had run out but now has a dose ahead of it becomes active
        // again. One the user paused deliberately stays paused.
        val status = if (
            existing.status == ScheduleStatus.COMPLETED &&
            DoseCalculator.nextIndexAt(draft, System.currentTimeMillis()) != null
        ) {
            ScheduleStatus.ACTIVE
        } else {
            existing.status
        }

        val updated = draft.copy(status = status)
        schedules.update(updated)
        // arm() cancels first, so the alarm armed against the old timing cannot survive.
        alarms.arm(updated)
    }

    companion object {
        fun factory(container: AppContainer, scheduleId: Long?) = viewModelFactory {
            initializer {
                ScheduleEditorViewModel(
                    scheduleId = scheduleId,
                    schedules = container.scheduleRepository,
                    doses = container.doseRepository,
                    alarms = container.alarmScheduler,
                )
            }
        }

        private fun blankForm(): ScheduleForm {
            val now = LocalTime.now()
            return ScheduleForm(
                name = "",
                epochDay = LocalDate.now().toEpochDay(),
                hour = now.hour,
                minute = 0,
                intervalMinutes = IntervalPreset.EIGHT_HOURLY.minutes,
                durationDays = 7,
            )
        }

        private fun Schedule.toForm(): ScheduleForm {
            val local = Instant.ofEpochMilli(startAtMillis).atZone(ZoneId.systemDefault())
            return ScheduleForm(
                name = name.orEmpty(),
                epochDay = local.toLocalDate().toEpochDay(),
                hour = local.hour,
                minute = local.minute,
                intervalMinutes = intervalMinutes,
                durationDays = durationDays,
            )
        }
    }
}
