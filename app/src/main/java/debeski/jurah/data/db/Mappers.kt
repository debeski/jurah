package debeski.jurah.data.db

import debeski.jurah.data.db.entity.DoseEventEntity
import debeski.jurah.data.db.entity.ScheduleEntity
import debeski.jurah.domain.model.DoseEvent
import debeski.jurah.domain.model.DoseStatus
import debeski.jurah.domain.model.Schedule
import debeski.jurah.domain.model.ScheduleStatus

fun ScheduleEntity.toDomain() = Schedule(
    id = id,
    name = name,
    startAtMillis = startAtMillis,
    intervalMinutes = intervalMinutes,
    durationDays = durationDays,
    status = enumOrDefault(status, ScheduleStatus.ACTIVE),
    createdAtMillis = createdAtMillis,
)

fun Schedule.toEntity() = ScheduleEntity(
    id = id,
    name = name,
    startAtMillis = startAtMillis,
    intervalMinutes = intervalMinutes,
    durationDays = durationDays,
    status = status.name,
    createdAtMillis = createdAtMillis,
)

fun DoseEventEntity.toDomain() = DoseEvent(
    id = id,
    scheduleId = scheduleId,
    doseIndex = doseIndex,
    scheduledAtMillis = scheduledAtMillis,
    actedAtMillis = actedAtMillis,
    status = enumOrDefault(status, DoseStatus.TAKEN),
)

fun DoseEvent.toEntity() = DoseEventEntity(
    id = id,
    scheduleId = scheduleId,
    doseIndex = doseIndex,
    scheduledAtMillis = scheduledAtMillis,
    actedAtMillis = actedAtMillis,
    status = status.name,
)

/** Stored enums are text, so a value written by a future version must not crash this one. */
private inline fun <reified T : Enum<T>> enumOrDefault(value: String, fallback: T): T =
    enumValues<T>().firstOrNull { it.name == value } ?: fallback
