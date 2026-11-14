package debeski.jurah.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import debeski.jurah.data.repo.DoseRepository
import debeski.jurah.data.repo.ScheduleRepository
import debeski.jurah.di.AppContainer
import debeski.jurah.domain.model.DoseStatus
import debeski.jurah.domain.schedule.DoseCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class HistoryStatus { TAKEN, SKIPPED, MISSED }

data class HistoryRow(
    val key: String,
    val scheduleName: String?,
    val doseNumber: Int,
    val scheduledAtMillis: Long,
    val actedAtMillis: Long?,
    val status: HistoryStatus,
)

data class HistoryUiState(
    val rows: List<HistoryRow> = emptyList(),
    val loaded: Boolean = false,
)

/**
 * History is assembled, not stored: taken and skipped doses come from recorded
 * events, and every elapsed slot without one is reported as missed. That way the
 * log shows what did not happen, which is the part that matters clinically.
 */
class HistoryViewModel(
    schedules: ScheduleRepository,
    doses: DoseRepository,
) : ViewModel() {

    val state: StateFlow<HistoryUiState> =
        combine(schedules.observeAll(), doses.observeAll()) { scheduleRows, events ->
            val now = System.currentTimeMillis()
            val windowStart = now - WINDOW_MILLIS
            val eventsBySchedule = events.groupBy { it.scheduleId }

            val rows = buildList {
                scheduleRows.forEach { schedule ->
                    val elapsed = DoseCalculator.elapsedIndices(schedule, now)
                    if (elapsed.isEmpty()) return@forEach
                    val acted = eventsBySchedule[schedule.id].orEmpty().associateBy { it.doseIndex }
                    val step = DoseCalculator.stepMillis(schedule)
                    // Start at the window edge rather than dose zero: a long-running
                    // course would otherwise walk thousands of slots on every emission.
                    val windowFirst = if (schedule.startAtMillis >= windowStart) {
                        0
                    } else {
                        ((windowStart - schedule.startAtMillis) / step).toInt()
                    }
                    for (index in maxOf(windowFirst, elapsed.first)..elapsed.last) {
                        val at = DoseCalculator.scheduledAt(schedule, index)
                        val event = acted[index]
                        when {
                            event != null -> add(
                                HistoryRow(
                                    key = "${schedule.id}-$index",
                                    scheduleName = schedule.name,
                                    doseNumber = index + 1,
                                    scheduledAtMillis = at,
                                    actedAtMillis = event.actedAtMillis,
                                    status = if (event.status == DoseStatus.TAKEN) {
                                        HistoryStatus.TAKEN
                                    } else {
                                        HistoryStatus.SKIPPED
                                    },
                                )
                            )

                            at < now - GRACE_MILLIS -> add(
                                HistoryRow(
                                    key = "${schedule.id}-$index",
                                    scheduleName = schedule.name,
                                    doseNumber = index + 1,
                                    scheduledAtMillis = at,
                                    actedAtMillis = null,
                                    status = HistoryStatus.MISSED,
                                )
                            )
                        }
                    }
                }
            }.sortedByDescending { it.scheduledAtMillis }.take(MAX_ROWS)

            HistoryUiState(rows = rows, loaded = true)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    companion object {
        private const val WINDOW_MILLIS = 90L * 24 * 60 * 60 * 1000
        private const val GRACE_MILLIS = 60 * 60 * 1000L
        private const val MAX_ROWS = 500

        fun factory(container: AppContainer) = viewModelFactory {
            initializer { HistoryViewModel(container.scheduleRepository, container.doseRepository) }
        }
    }
}
