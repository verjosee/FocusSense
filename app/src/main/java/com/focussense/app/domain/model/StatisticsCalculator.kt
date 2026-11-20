package com.focussense.app.domain.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object StatisticsCalculator {

    fun calculateWeeklyBreakdown(
        sessions: List<FocusSession>,
        referenceTimestamp: Long = System.currentTimeMillis()
    ): List<DayStudyStat> {
        val result = mutableListOf<DayStudyStat>()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

        val calendar = Calendar.getInstance().apply {
            timeInMillis = referenceTimestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Generate past 7 days (from 6 days ago up to today)
        val dayStartTimes = LongArray(7)
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        for (i in 0 until 7) {
            dayStartTimes[i] = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        for (dayStart in dayStartTimes) {
            val dayEnd = dayStart + 24 * 60 * 60 * 1000L - 1
            val daySessions = sessions.filter { it.startTime in dayStart..dayEnd }
            val totalSeconds = daySessions.sumOf { it.durationSeconds }
            val totalMinutes = (totalSeconds / 60).toInt()

            val dayCal = Calendar.getInstance().apply { timeInMillis = dayStart }
            val dayLabel = dayFormat.format(dayCal.time)

            result.add(
                DayStudyStat(
                    dayName = dayLabel,
                    durationMinutes = totalMinutes,
                    dateTimestamp = dayStart
                )
            )
        }

        return result
    }

    fun calculateSummary(
        sessions: List<FocusSession>,
        referenceTimestamp: Long = System.currentTimeMillis()
    ): StatisticsSummary {
        val totalDurationSeconds = sessions.sumOf { it.durationSeconds }
        val totalCompletedSessions = sessions.count { it.status == SessionStatus.COMPLETED }
        val totalSessions = sessions.size

        val averageDurationSeconds = if (totalSessions > 0) {
            totalDurationSeconds / totalSessions
        } else {
            0L
        }

        val todayCalendar = Calendar.getInstance().apply {
            timeInMillis = referenceTimestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = todayCalendar.timeInMillis
        val endOfToday = startOfToday + 24 * 60 * 60 * 1000L - 1

        val todaySessions = sessions.filter { it.startTime in startOfToday..endOfToday }
        val todayDurationSeconds = todaySessions.sumOf { it.durationSeconds }
        val todayCompletedSessions = todaySessions.count { it.status == SessionStatus.COMPLETED }

        val weeklyBreakdown = calculateWeeklyBreakdown(sessions, referenceTimestamp)

        return StatisticsSummary(
            totalDurationSeconds = totalDurationSeconds,
            todayDurationSeconds = todayDurationSeconds,
            totalCompletedSessions = totalCompletedSessions,
            todayCompletedSessions = todayCompletedSessions,
            averageDurationSeconds = averageDurationSeconds,
            dailyBreakdown = weeklyBreakdown
        )
    }

    fun calculateCompletionRate(sessions: List<FocusSession>): Float {
        if (sessions.isEmpty()) return 0f
        val completed = sessions.count { it.status == SessionStatus.COMPLETED }
        return (completed.toFloat() / sessions.size.toFloat()) * 100f
    }
}
