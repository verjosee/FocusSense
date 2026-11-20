package com.focussense.app.domain.model

data class FocusSession(
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long? = null,
    val durationSeconds: Long,
    val status: SessionStatus,
    val notes: String? = null,
    val movementCount: Int = 0,
    val averageLightLux: Float? = null
) {
    val durationMinutes: Int
        get() = (durationSeconds / 60).toInt()

    val formattedDuration: String
        get() {
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            return if (minutes >= 60) {
                val hours = minutes / 60
                val remainingMinutes = minutes % 60
                String.format("%dh %02dm", hours, remainingMinutes)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }
}
