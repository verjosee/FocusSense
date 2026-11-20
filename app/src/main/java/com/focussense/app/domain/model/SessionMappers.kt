package com.focussense.app.domain.model

import com.focussense.app.data.local.entity.StudySessionEntity

fun StudySessionEntity.toDomain(): FocusSession {
    return FocusSession(
        id = id,
        startTime = startTime,
        endTime = endTime,
        durationSeconds = durationSeconds,
        status = SessionStatus.fromString(status),
        notes = notes,
        movementCount = movementCount,
        averageLightLux = averageLightLux
    )
}

fun FocusSession.toEntity(): StudySessionEntity {
    return StudySessionEntity(
        id = id,
        startTime = startTime,
        endTime = endTime,
        durationSeconds = durationSeconds,
        status = status.name,
        notes = notes,
        movementCount = movementCount,
        averageLightLux = averageLightLux
    )
}

fun List<StudySessionEntity>.toDomainList(): List<FocusSession> {
    return map { it.toDomain() }
}
