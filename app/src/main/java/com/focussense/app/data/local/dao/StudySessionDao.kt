package com.focussense.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.focussense.app.data.local.entity.StudySessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {

    @Query("SELECT * FROM study_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE id = :id")
    fun getSessionById(id: Long): Flow<StudySessionEntity?>

    @Query("SELECT * FROM study_sessions WHERE startTime >= :startTime AND startTime <= :endTime ORDER BY startTime DESC")
    fun getSessionsInRange(startTime: Long, endTime: Long): Flow<List<StudySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySessionEntity): Long

    @Update
    suspend fun updateSession(session: StudySessionEntity): Int

    @Query("UPDATE study_sessions SET notes = :notes WHERE id = :id")
    suspend fun updateNotes(id: Long, notes: String?): Int

    @Delete
    suspend fun deleteSession(session: StudySessionEntity): Int

    @Query("DELETE FROM study_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long): Int

    @Query("DELETE FROM study_sessions")
    suspend fun clearAllSessions(): Int

    @Query("SELECT COUNT(*) FROM study_sessions WHERE startTime >= :startOfDay AND status = 'COMPLETED'")
    fun getTodayCompletedCount(startOfDay: Long): Flow<Int>

    @Query("SELECT SUM(durationSeconds) FROM study_sessions WHERE startTime >= :startOfDay AND status = 'COMPLETED'")
    fun getTodayTotalDuration(startOfDay: Long): Flow<Long?>

    @Query("SELECT SUM(durationSeconds) FROM study_sessions WHERE status = 'COMPLETED'")
    fun getTotalStudyDuration(): Flow<Long?>

    @Query("SELECT COUNT(*) FROM study_sessions WHERE status = 'COMPLETED'")
    fun getCompletedSessionsCount(): Flow<Int>
}
