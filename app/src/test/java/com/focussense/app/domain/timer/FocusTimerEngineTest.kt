package com.focussense.app.domain.timer

import com.focussense.app.domain.model.SessionStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeTimeProvider(
    var elapsedMs: Long = 100_000L,
    var currentMs: Long = 1_700_000_000_000L
) : TimeProvider {
    override fun elapsedRealtime(): Long = elapsedMs
    override fun currentTimeMillis(): Long = currentMs

    fun advanceTime(seconds: Long) {
        elapsedMs += seconds * 1000L
        currentMs += seconds * 1000L
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class FocusTimerEngineTest {

    private lateinit var fakeTimeProvider: FakeTimeProvider
    private lateinit var testDispatcher: TestDispatcher
    private lateinit var testScope: TestScope
    private lateinit var timerEngine: DefaultFocusTimerEngine

    @Before
    fun setUp() {
        fakeTimeProvider = FakeTimeProvider()
        testDispatcher = StandardTestDispatcher()
        testScope = TestScope(testDispatcher)
        timerEngine = DefaultFocusTimerEngine(
            sensorRepository = null,
            timeProvider = fakeTimeProvider,
            engineScope = testScope
        )
    }

    @After
    fun tearDown() {
        timerEngine.reset()
    }

    @Test
    fun testInitialStateIsIdle() {
        val state = timerEngine.timerState.value
        assertEquals(TimerMode.IDLE, state.mode)
        assertEquals(25 * 60L, state.remainingSeconds)
        assertEquals(0.0f, state.progress, 0.001f)
        assertEquals("25:00", state.formattedRemainingTime)
        assertFalse(state.isRunning)
        assertTrue(state.isIdle)
    }

    @Test
    fun testStartTransitionsToRunning() {
        timerEngine.start(25)
        val state = timerEngine.timerState.value
        assertEquals(TimerMode.RUNNING, state.mode)
        assertTrue(state.isRunning)
        assertEquals(25 * 60L, state.targetDurationSeconds)
        assertEquals(25 * 60L, state.remainingSeconds)
        assertEquals(0L, state.elapsedSeconds)
        timerEngine.reset()
    }

    @Test
    fun testTickingUpdatesElapsedAndRemaining() = runTest(testDispatcher) {
        timerEngine.start(25)
        fakeTimeProvider.advanceTime(10)

        // Advance coroutine delay
        advanceTimeBy(300L)

        val state = timerEngine.timerState.value
        assertEquals(TimerMode.RUNNING, state.mode)
        assertEquals(10L, state.elapsedSeconds)
        assertEquals(25 * 60L - 10L, state.remainingSeconds)
        assertEquals("24:50", state.formattedRemainingTime)
        timerEngine.reset()
    }

    @Test
    fun testPauseAndResume() = runTest(testDispatcher) {
        timerEngine.start(25)
        fakeTimeProvider.advanceTime(15)
        advanceTimeBy(300L)

        timerEngine.pause()
        val pausedState = timerEngine.timerState.value
        assertEquals(TimerMode.PAUSED, pausedState.mode)
        assertTrue(pausedState.isPaused)
        assertEquals(15L, pausedState.elapsedSeconds)

        // Time passes while paused
        fakeTimeProvider.advanceTime(30)
        advanceTimeBy(300L)

        // Elapsed should still be 15s because timer was paused
        assertEquals(15L, timerEngine.timerState.value.elapsedSeconds)

        // Resume timer
        timerEngine.resume()
        assertEquals(TimerMode.RUNNING, timerEngine.timerState.value.mode)

        fakeTimeProvider.advanceTime(5)
        advanceTimeBy(300L)

        assertEquals(20L, timerEngine.timerState.value.elapsedSeconds)
        assertEquals(25 * 60L - 20L, timerEngine.timerState.value.remainingSeconds)
        timerEngine.reset()
    }

    @Test
    fun testStopReturnsInterruptedSession() = runTest(testDispatcher) {
        timerEngine.start(25)
        fakeTimeProvider.advanceTime(60)

        val session = timerEngine.stop()
        assertNotNull(session)
        assertEquals(SessionStatus.INTERRUPTED, session!!.status)
        assertEquals(60L, session.durationSeconds)

        val finalState = timerEngine.timerState.value
        assertEquals(TimerMode.IDLE, finalState.mode)
        timerEngine.reset()
    }

    @Test
    fun testCompleteSetsCompletedStateAndBuildsSession() = runTest(testDispatcher) {
        timerEngine.start(1)
        fakeTimeProvider.advanceTime(60)

        val completedSession = timerEngine.complete()
        assertNotNull(completedSession)
        assertEquals(SessionStatus.COMPLETED, completedSession!!.status)

        val state = timerEngine.timerState.value
        assertEquals(TimerMode.COMPLETED, state.mode)
        assertEquals(0L, state.remainingSeconds)
        assertEquals(1.0f, state.progress, 0.001f)
        assertEquals("00:00", state.formattedRemainingTime)
        timerEngine.reset()
    }

    @Test
    fun testPreventsDuplicateSaves() {
        timerEngine.start(25)
        fakeTimeProvider.advanceTime(30)

        val firstSession = timerEngine.buildAndConsumeSession(SessionStatus.COMPLETED, "Great focus")
        assertNotNull(firstSession)
        assertEquals("Great focus", firstSession!!.notes)

        // Second attempt to consume or save must return null
        val secondSession = timerEngine.buildAndConsumeSession(SessionStatus.COMPLETED, "Duplicate")
        assertNull(secondSession)
        timerEngine.reset()
    }

    @Test
    fun testResetClearsStateToDefault() {
        timerEngine.start(45)
        fakeTimeProvider.advanceTime(100)

        timerEngine.reset(15)
        val state = timerEngine.timerState.value
        assertEquals(TimerMode.IDLE, state.mode)
        assertEquals(15 * 60L, state.targetDurationSeconds)
        assertEquals(15 * 60L, state.remainingSeconds)
        assertEquals(0L, state.elapsedSeconds)
        assertEquals("15:00", state.formattedRemainingTime)
    }
}
