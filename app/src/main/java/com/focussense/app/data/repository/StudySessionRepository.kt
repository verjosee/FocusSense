package com.focussense.app.data.repository

import com.focussense.app.data.local.entity.StudySessionEntity
import kotlinx.coroutines.flow.Flow

interface StudySessionRepository {
    fun getAllSessions(): Flow<List<StudySessionEntity>>
    fun getSessionById(id: Long): Flow<StudySessionEntity?>
    fun getSessionsInRange(startTime: Long, endTime: Long): Flow<List<StudySessionEntity>>
    suspend fun insertSession(session: StudySessionEntity): Long
    suspend fun updateSession(session: StudySessionEntity)
    suspend fun updateNotes(id: Long, notes: String?)
    suspend fun deleteSession(session: StudySessionEntity)
    suspend fun deleteSessionById(id: Long)
    suspend fun clearAllSessions()
    fun getTodayCompletedCount(startOfDay: Long): Flow<Int>
    fun getTodayTotalDuration(startOfDay: Long): Flow<Long?>
    fun getTotalStudyDuration(): Flow<Long?>
    fun getCompletedSessionsCount(): Flow<Int>
}
