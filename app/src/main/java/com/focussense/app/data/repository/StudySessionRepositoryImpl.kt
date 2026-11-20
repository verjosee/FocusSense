package com.focussense.app.data.repository

import com.focussense.app.data.local.dao.StudySessionDao
import com.focussense.app.data.local.entity.StudySessionEntity
import kotlinx.coroutines.flow.Flow

class StudySessionRepositoryImpl(
    private val sessionDao: StudySessionDao
) : StudySessionRepository {

    override fun getAllSessions(): Flow<List<StudySessionEntity>> =
        sessionDao.getAllSessions()

    override fun getSessionById(id: Long): Flow<StudySessionEntity?> =
        sessionDao.getSessionById(id)

    override fun getSessionsInRange(startTime: Long, endTime: Long): Flow<List<StudySessionEntity>> =
        sessionDao.getSessionsInRange(startTime, endTime)

    override suspend fun insertSession(session: StudySessionEntity): Long =
        sessionDao.insertSession(session)

    override suspend fun updateSession(session: StudySessionEntity) {
        sessionDao.updateSession(session)
    }

    override suspend fun updateNotes(id: Long, notes: String?) {
        sessionDao.updateNotes(id, notes)
    }

    override suspend fun deleteSession(session: StudySessionEntity) {
        sessionDao.deleteSession(session)
    }

    override suspend fun deleteSessionById(id: Long) {
        sessionDao.deleteSessionById(id)
    }

    override suspend fun clearAllSessions() {
        sessionDao.clearAllSessions()
    }

    override fun getTodayCompletedCount(startOfDay: Long): Flow<Int> =
        sessionDao.getTodayCompletedCount(startOfDay)

    override fun getTodayTotalDuration(startOfDay: Long): Flow<Long?> =
        sessionDao.getTodayTotalDuration(startOfDay)

    override fun getTotalStudyDuration(): Flow<Long?> =
        sessionDao.getTotalStudyDuration()

    override fun getCompletedSessionsCount(): Flow<Int> =
        sessionDao.getCompletedSessionsCount()
}
