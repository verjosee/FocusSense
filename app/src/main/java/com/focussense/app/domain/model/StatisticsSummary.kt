package com.focussense.app.domain.model

data class StatisticsSummary(
    val totalDurationSeconds: Long = 0L,
    val todayDurationSeconds: Long = 0L,
    val totalCompletedSessions: Int = 0,
    val todayCompletedSessions: Int = 0,
    val averageDurationSeconds: Long = 0L,
    val dailyBreakdown: List<DayStudyStat> = emptyList()
) {
    val totalDurationMinutes: Int
        get() = (totalDurationSeconds / 60).toInt()

    val todayDurationMinutes: Int
        get() = (todayDurationSeconds / 60).toInt()

    val averageDurationMinutes: Int
        get() = (averageDurationSeconds / 60).toInt()

    val formattedTotalDuration: String
        get() = formatDuration(totalDurationSeconds)

    val formattedTodayDuration: String
        get() = formatDuration(todayDurationSeconds)

    val formattedAverageDuration: String
        get() = formatDuration(averageDurationSeconds)

    companion object {
        fun formatDuration(seconds: Long): String {
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            val remainingSecs = seconds % 60
            return when {
                hours > 0 -> "${hours}h ${minutes}m"
                minutes > 0 -> "${minutes}m ${remainingSecs}s"
                else -> "${remainingSecs}s"
            }
        }
    }
}
