package com.focussense.app.data.repository

import com.focussense.app.data.local.dao.StudySessionDao
import com.focussense.app.data.local.entity.StudySessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StudySessionRepositoryTest {

    private lateinit var fakeDao: FakeStudySessionDao
    private lateinit var repository: StudySessionRepository

    @Before
    fun setUp() {
        fakeDao = FakeStudySessionDao()
        repository = StudySessionRepositoryImpl(fakeDao)
    }

    @Test
    fun insertAndGetAllSessions() = runTest {
        val session1 = StudySessionEntity(
            id = 1L,
            startTime = 1000L,
            endTime = 2500L,
            durationSeconds = 1500L,
            status = StudySessionEntity.STATUS_COMPLETED,
            notes = "First session",
            movementCount = 2,
            averageLightLux = 350f
        )
        val id = repository.insertSession(session1)
        assertEquals(1L, id)

        val sessions = repository.getAllSessions().first()
        assertEquals(1, sessions.size)
        assertEquals("First session", sessions[0].notes)
    }

    @Test
    fun getSessionsInRange() = runTest {
        val session1 = StudySessionEntity(id = 1L, startTime = 1000L, durationSeconds = 500L, status = "COMPLETED")
        val session2 = StudySessionEntity(id = 2L, startTime = 3000L, durationSeconds = 500L, status = "COMPLETED")
        repository.insertSession(session1)
        repository.insertSession(session2)

        val filtered = repository.getSessionsInRange(500L, 2000L).first()
        assertEquals(1, filtered.size)
        assertEquals(1L, filtered[0].id)
    }

    @Test
    fun updateSessionAndNotes() = runTest {
        val session = StudySessionEntity(id = 1L, startTime = 1000L, durationSeconds = 1500L, status = "COMPLETED", notes = "Old")
        repository.insertSession(session)

        repository.updateNotes(1L, "Updated Notes")
        val updated = repository.getSessionById(1L).first()
        assertNotNull(updated)
        assertEquals("Updated Notes", updated?.notes)
    }

    @Test
    fun deleteSessionByIdAndClearAll() = runTest {
        val session1 = StudySessionEntity(id = 1L, startTime = 1000L, durationSeconds = 500L, status = "COMPLETED")
        val session2 = StudySessionEntity(id = 2L, startTime = 2000L, durationSeconds = 500L, status = "COMPLETED")
        repository.insertSession(session1)
        repository.insertSession(session2)

        repository.deleteSessionById(1L)
        assertEquals(1, repository.getAllSessions().first().size)

        repository.clearAllSessions()
        assertTrue(repository.getAllSessions().first().isEmpty())
    }

    @Test
    fun todayStatisticsCalculations() = runTest {
        val session1 = StudySessionEntity(id = 1L, startTime = 5000L, durationSeconds = 1500L, status = "COMPLETED")
        val session2 = StudySessionEntity(id = 2L, startTime = 6000L, durationSeconds = 900L, status = "COMPLETED")
        val session3 = StudySessionEntity(id = 3L, startTime = 7000L, durationSeconds = 300L, status = "CANCELLED")
        repository.insertSession(session1)
        repository.insertSession(session2)
        repository.insertSession(session3)

        val completedCount = repository.getTodayCompletedCount(4000L).first()
        assertEquals(2, completedCount)

        val totalDuration = repository.getTodayTotalDuration(4000L).first()
        assertEquals(2400L, totalDuration)

        val allDuration = repository.getTotalStudyDuration().first()
        assertEquals(2400L, allDuration)
    }
}

class FakeStudySessionDao : StudySessionDao {
    private val sessions = mutableListOf<StudySessionEntity>()

    override fun getAllSessions(): Flow<List<StudySessionEntity>> =
        flowOf(sessions.sortedByDescending { it.startTime })

    override fun getSessionById(id: Long): Flow<StudySessionEntity?> =
        flowOf(sessions.find { it.id == id })

    override fun getSessionsInRange(startTime: Long, endTime: Long): Flow<List<StudySessionEntity>> =
        flowOf(sessions.filter { it.startTime in startTime..endTime }.sortedByDescending { it.startTime })

    override suspend fun insertSession(session: StudySessionEntity): Long {
        sessions.removeAll { it.id == session.id }
        sessions.add(session)
        return session.id
    }

    override suspend fun updateSession(session: StudySessionEntity): Int {
        val index = sessions.indexOfFirst { it.id == session.id }
        if (index != -1) {
            sessions[index] = session
            return 1
        }
        return 0
    }

    override suspend fun updateNotes(id: Long, notes: String?): Int {
        val index = sessions.indexOfFirst { it.id == id }
        if (index != -1) {
            sessions[index] = sessions[index].copy(notes = notes)
            return 1
        }
        return 0
    }

    override suspend fun deleteSession(session: StudySessionEntity): Int {
        return if (sessions.removeIf { it.id == session.id }) 1 else 0
    }

    override suspend fun deleteSessionById(id: Long): Int {
        return if (sessions.removeIf { it.id == id }) 1 else 0
    }

    override suspend fun clearAllSessions(): Int {
        val count = sessions.size
        sessions.clear()
        return count
    }

    override fun getTodayCompletedCount(startOfDay: Long): Flow<Int> =
        flowOf(sessions.count { it.startTime >= startOfDay && it.status == "COMPLETED" })

    override fun getTodayTotalDuration(startOfDay: Long): Flow<Long?> =
        flowOf(sessions.filter { it.startTime >= startOfDay && it.status == "COMPLETED" }.sumOf { it.durationSeconds }.takeIf { it > 0 })

    override fun getTotalStudyDuration(): Flow<Long?> =
        flowOf(sessions.filter { it.status == "COMPLETED" }.sumOf { it.durationSeconds }.takeIf { it > 0 })

    override fun getCompletedSessionsCount(): Flow<Int> =
        flowOf(sessions.count { it.status == "COMPLETED" })
}
