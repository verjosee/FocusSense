package com.focussense.app.domain.timer

import com.focussense.app.domain.model.SessionStatus
import com.focussense.app.sensors.SensorRepository
import com.focussense.app.sensors.model.AccelerationData
import com.focussense.app.sensors.model.LightCategory
import com.focussense.app.sensors.model.LightData
import com.focussense.app.sensors.model.ProximityData
import com.focussense.app.sensors.model.SecondarySensorType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
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

class FakeSensorRepository(
    initialAcceleration: AccelerationData = AccelerationData(),
    initialLight: LightData = LightData(),
    initialProximity: ProximityData = ProximityData()
) : SensorRepository {
    private val _accelFlow = MutableStateFlow(initialAcceleration)
    override val accelerometerFlow: StateFlow<AccelerationData> = _accelFlow

    private val _lightFlow = MutableStateFlow(initialLight)
    override val lightFlow: StateFlow<LightData> = _lightFlow

    private val _proxFlow = MutableStateFlow(initialProximity)
    override val proximityFlow: StateFlow<ProximityData> = _proxFlow

    override val isAccelerometerAvailable: Boolean = true
    override val isLightSensorAvailable: Boolean = true
    override val isProximitySensorAvailable: Boolean = true
    override val proximitySensorName: String = "Test Proximity"
    override val activeSecondarySensorType: SecondarySensorType = SecondarySensorType.PROXIMITY

    var isListening: Boolean = false
        private set
    var startListeningCallCount: Int = 0
        private set
    var stopListeningCallCount: Int = 0
        private set

    override fun startListening() {
        isListening = true
        startListeningCallCount++
    }

    override fun stopListening() {
        isListening = false
        stopListeningCallCount++
    }

    override fun setMovementThreshold(threshold: Float) {}

    fun emitAcceleration(data: AccelerationData) {
        _accelFlow.value = data
    }

    fun emitLight(data: LightData) {
        _lightFlow.value = data
    }

    fun emitProximity(data: ProximityData) {
        _proxFlow.value = data
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class FocusTimerEngineTest {

    private lateinit var fakeTimeProvider: FakeTimeProvider

    @Before
    fun setUp() {
        fakeTimeProvider = FakeTimeProvider()
    }

    @Test
    fun testInitialStateIsIdle() {
        val timerEngine = DefaultFocusTimerEngine(timeProvider = fakeTimeProvider)
        val state = timerEngine.timerState.value
        assertEquals(TimerMode.IDLE, state.mode)
        assertEquals(25 * 60L, state.remainingSeconds)
        assertEquals(0.0f, state.progress, 0.001f)
        assertEquals("25:00", state.formattedRemainingTime)
        assertFalse(state.isRunning)
        assertTrue(state.isIdle)
    }

    @Test
    fun testStartTransitionsToRunning() = runTest {
        val timerEngine = DefaultFocusTimerEngine(
            timeProvider = fakeTimeProvider,
            engineScope = backgroundScope
        )
        timerEngine.start(25)
        val state = timerEngine.timerState.value
        assertEquals(TimerMode.RUNNING, state.mode)
        assertTrue(state.isRunning)
        assertEquals(25 * 60L, state.targetDurationSeconds)
        assertEquals(25 * 60L, state.remainingSeconds)
        assertEquals(0L, state.elapsedSeconds)
    }

    @Test
    fun testTickingUpdatesElapsedAndRemaining() = runTest {
        val timerEngine = DefaultFocusTimerEngine(
            timeProvider = fakeTimeProvider,
            engineScope = backgroundScope
        )
        timerEngine.start(25)
        fakeTimeProvider.advanceTime(10)

        // Advance coroutine virtual delay
        advanceTimeBy(300L)

        val state = timerEngine.timerState.value
        assertEquals(TimerMode.RUNNING, state.mode)
        assertEquals(10L, state.elapsedSeconds)
        assertEquals(25 * 60L - 10L, state.remainingSeconds)
        assertEquals("24:50", state.formattedRemainingTime)
    }

    @Test
    fun testPauseAndResume() = runTest {
        val timerEngine = DefaultFocusTimerEngine(
            timeProvider = fakeTimeProvider,
            engineScope = backgroundScope
        )
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
    }

    @Test
    fun testStopReturnsInterruptedSession() = runTest {
        val timerEngine = DefaultFocusTimerEngine(
            timeProvider = fakeTimeProvider,
            engineScope = backgroundScope
        )
        timerEngine.start(25)
        fakeTimeProvider.advanceTime(60)

        val session = timerEngine.stop()
        assertNotNull(session)
        assertEquals(SessionStatus.INTERRUPTED, session!!.status)
        assertEquals(60L, session.durationSeconds)

        val finalState = timerEngine.timerState.value
        assertEquals(TimerMode.IDLE, finalState.mode)
    }

    @Test
    fun testCompleteSetsCompletedStateAndBuildsSession() = runTest {
        val timerEngine = DefaultFocusTimerEngine(
            timeProvider = fakeTimeProvider,
            engineScope = backgroundScope
        )
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
    }

    @Test
    fun testPreventsDuplicateSaves() = runTest {
        val timerEngine = DefaultFocusTimerEngine(
            timeProvider = fakeTimeProvider,
            engineScope = backgroundScope
        )
        timerEngine.start(25)
        fakeTimeProvider.advanceTime(30)

        val firstSession = timerEngine.buildAndConsumeSession(SessionStatus.COMPLETED, "Great focus")
        assertNotNull(firstSession)
        assertEquals("Great focus", firstSession!!.notes)

        // Second attempt to consume or save must return null
        val secondSession = timerEngine.buildAndConsumeSession(SessionStatus.COMPLETED, "Duplicate")
        assertNull(secondSession)
    }

    @Test
    fun testResetClearsStateToDefault() = runTest {
        val timerEngine = DefaultFocusTimerEngine(
            timeProvider = fakeTimeProvider,
            engineScope = backgroundScope
        )
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

    // --- Sensor Integration Tests ---

    @Test
    fun testSensorMovementDetectionIncrementsCountAndTriggersAlert() = runTest {
        val fakeRepo = FakeSensorRepository()
        val engineWithSensors = DefaultFocusTimerEngine(
            sensorRepository = fakeRepo,
            timeProvider = fakeTimeProvider,
            engineScope = backgroundScope
        )

        engineWithSensors.start(25)
        advanceTimeBy(100L)
        assertEquals(0, engineWithSensors.timerState.value.movementCount)
        assertFalse(engineWithSensors.timerState.value.isMotionAlertActive)

        // Emit significant movement
        fakeTimeProvider.advanceTime(2)
        fakeRepo.emitAcceleration(AccelerationData(isSignificantMovement = true))
        advanceTimeBy(100L)

        assertEquals(1, engineWithSensors.timerState.value.movementCount)
        assertTrue(engineWithSensors.timerState.value.isMotionAlertActive)

        // Dismiss alert
        engineWithSensors.dismissMotionAlert()
        assertFalse(engineWithSensors.timerState.value.isMotionAlertActive)
    }

    @Test
    fun testSensorLightCollectionComputesAverageLux() = runTest {
        val fakeRepo = FakeSensorRepository(
            initialLight = LightData(lux = 100f, category = LightCategory.DIM)
        )
        val engineWithSensors = DefaultFocusTimerEngine(
            sensorRepository = fakeRepo,
            timeProvider = fakeTimeProvider,
            engineScope = backgroundScope
        )

        engineWithSensors.start(25)
        advanceTimeBy(100L)
        assertEquals(100f, engineWithSensors.timerState.value.currentLux ?: 0f, 0.01f)
        assertEquals(100f, engineWithSensors.timerState.value.averageLightLux ?: 0f, 0.01f)

        fakeRepo.emitLight(LightData(lux = 300f, category = LightCategory.MODERATE))
        advanceTimeBy(100L)
        assertEquals(300f, engineWithSensors.timerState.value.currentLux ?: 0f, 0.01f)
        // Average of 100 and 300 = 200
        assertEquals(200f, engineWithSensors.timerState.value.averageLightLux ?: 0f, 0.01f)

        val session = engineWithSensors.stop()
        assertNotNull(session)
        assertEquals(200f, session!!.averageLightLux ?: 0f, 0.01f)
    }

    @Test
    fun testSensorProximityCollectionUpdatesTimerState() = runTest {
        val fakeRepo = FakeSensorRepository()
        val engineWithSensors = DefaultFocusTimerEngine(
            sensorRepository = fakeRepo,
            timeProvider = fakeTimeProvider,
            engineScope = backgroundScope
        )

        engineWithSensors.start(25)
        advanceTimeBy(100L)

        // Initial default: isProximitySupported is true
        assertTrue(engineWithSensors.timerState.value.isProximitySupported)
        assertFalse(engineWithSensors.timerState.value.isProximityNear)

        // Emit Near event (distance 0.0cm)
        fakeRepo.emitProximity(ProximityData(distance = 0.0f, maxRange = 5.0f, isNear = true))
        advanceTimeBy(100L)
        assertTrue(engineWithSensors.timerState.value.isProximityNear)
        assertEquals(0.0f, engineWithSensors.timerState.value.proximityDistance ?: -1f, 0.01f)

        // Emit Far event (distance 5.0cm)
        fakeRepo.emitProximity(ProximityData(distance = 5.0f, maxRange = 5.0f, isNear = false))
        advanceTimeBy(100L)
        assertFalse(engineWithSensors.timerState.value.isProximityNear)
        assertEquals(5.0f, engineWithSensors.timerState.value.proximityDistance ?: -1f, 0.01f)
    }

    @Test
    fun testCompleteThenBuildAndConsumeSessionReturnsValidSession() = runTest {
        val timerEngine = DefaultFocusTimerEngine(
            timeProvider = fakeTimeProvider,
            engineScope = backgroundScope
        )
        timerEngine.start(25)
        fakeTimeProvider.advanceTime(25 * 60L)

        // Complete called (e.g. by tickJob or engine completion)
        val completedSession = timerEngine.complete()
        assertNotNull(completedSession)
        assertEquals(SessionStatus.COMPLETED, completedSession!!.status)

        // ViewModel consumes session for save dialog
        val consumedByViewModel = timerEngine.buildAndConsumeSession(SessionStatus.COMPLETED, "Great study session")
        assertNotNull(consumedByViewModel)
        assertEquals("Great study session", consumedByViewModel!!.notes)
        assertEquals(SessionStatus.COMPLETED, consumedByViewModel.status)

        // Subsequent attempt to consume must return null (protection against duplicate saves)
        val duplicateAttempt = timerEngine.buildAndConsumeSession(SessionStatus.COMPLETED)
        assertNull(duplicateAttempt)
    }
}
