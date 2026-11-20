package com.focussense.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long? = null,
    val durationSeconds: Long,
    val status: String, // "COMPLETED", "INTERRUPTED", "CANCELLED"
    val notes: String? = null,
    val movementCount: Int = 0,
    val averageLightLux: Float? = null
) {
    companion object {
        const val STATUS_COMPLETED = "COMPLETED"
        const val STATUS_INTERRUPTED = "INTERRUPTED"
        const val STATUS_CANCELLED = "CANCELLED"
    }
}
