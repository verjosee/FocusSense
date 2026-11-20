package com.focussense.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class StatisticsCalculationTest {

    @Test
    fun testWeeklyBreakdownReturns7Days() {
        val now = System.currentTimeMillis()
        val breakdown = StatisticsCalculator.calculateWeeklyBreakdown(emptyList(), now)
        assertEquals(7, breakdown.size)
        // Ensure all day names are non-empty
        for (day in breakdown) {
            assertTrue(day.dayName.isNotBlank())
            assertEquals(0, day.durationMinutes)
        }
    }

    @Test
    fun testWeeklyBreakdownAggregatesCorrectDayMinutes() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayTime = calendar.timeInMillis

        // Session today: 25 minutes (1500 seconds)
        val sessionToday1 = FocusSession(
            id = 1,
            startTime = todayTime,
            endTime = todayTime + 1500 * 1000L,
            durationSeconds = 1500,
            status = SessionStatus.COMPLETED
        )

        // Session today: 15 minutes (900 seconds)
        val sessionToday2 = FocusSession(
            id = 2,
            startTime = todayTime + 2000 * 1000L,
            endTime = todayTime + 2900 * 1000L,
            durationSeconds = 900,
            status = SessionStatus.COMPLETED
        )

        val breakdown = StatisticsCalculator.calculateWeeklyBreakdown(
            listOf(sessionToday1, sessionToday2),
            todayTime
        )

        val todayStat = breakdown.last()
        assertEquals(40, todayStat.durationMinutes)
    }

    @Test
    fun testCompletionRateCalculation() {
        // Empty list -> 0%
        assertEquals(0f, StatisticsCalculator.calculateCompletionRate(emptyList()), 0.001f)

        val sessions = listOf(
            FocusSession(id = 1, startTime = 1000, durationSeconds = 1500, status = SessionStatus.COMPLETED),
            FocusSession(id = 2, startTime = 2000, durationSeconds = 600, status = SessionStatus.INTERRUPTED),
            FocusSession(id = 3, startTime = 3000, durationSeconds = 1500, status = SessionStatus.COMPLETED),
            FocusSession(id = 4, startTime = 4000, durationSeconds = 300, status = SessionStatus.CANCELLED)
        )

        // 2 out of 4 = 50%
        val rate = StatisticsCalculator.calculateCompletionRate(sessions)
        assertEquals(50.0f, rate, 0.001f)
    }

    @Test
    fun testSummaryAggregation() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val now = calendar.timeInMillis

        val sessions = listOf(
            FocusSession(id = 1, startTime = now, durationSeconds = 1200, status = SessionStatus.COMPLETED), // 20m
            FocusSession(id = 2, startTime = now + 2000, durationSeconds = 1800, status = SessionStatus.COMPLETED) // 30m
        )

        val summary = StatisticsCalculator.calculateSummary(sessions, now)

        assertEquals(3000L, summary.totalDurationSeconds)
        assertEquals(3000L, summary.todayDurationSeconds)
        assertEquals(2, summary.totalCompletedSessions)
        assertEquals(2, summary.todayCompletedSessions)
        assertEquals(1500L, summary.averageDurationSeconds)
        assertEquals("50m 0s", summary.formattedTotalDuration)
        assertEquals(7, summary.dailyBreakdown.size)
    }

    @Test
    fun testDurationFormatting() {
        assertEquals("0s", StatisticsSummary.formatDuration(0))
        assertEquals("45s", StatisticsSummary.formatDuration(45))
        assertEquals("25m 0s", StatisticsSummary.formatDuration(1500))
        assertEquals("1h 15m", StatisticsSummary.formatDuration(4500))
    }
}
