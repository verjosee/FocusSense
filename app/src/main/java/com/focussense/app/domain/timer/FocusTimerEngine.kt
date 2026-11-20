package com.focussense.app.domain.timer

import com.focussense.app.domain.model.FocusSession
import com.focussense.app.domain.model.SessionStatus
import com.focussense.app.sensors.SensorRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

interface FocusTimerEngine {
    val timerState: StateFlow<TimerState>

    fun start(durationMinutes: Int)
    fun pause()
    fun resume()
    fun stop(): FocusSession?
    fun complete(): FocusSession?
    fun reset(targetDurationMinutes: Int = 25)
    fun buildAndConsumeSession(status: SessionStatus, notes: String? = null): FocusSession?
    fun dismissMotionAlert()
}

class DefaultFocusTimerEngine(
    private val sensorRepository: SensorRepository? = null,
    private val timeProvider: TimeProvider = DefaultTimeProvider(),
    private val engineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : FocusTimerEngine {

    private val _timerState = MutableStateFlow(TimerState())
    override val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private var tickJob: Job? = null
    private var accelerometerJob: Job? = null
    private var lightJob: Job? = null
    private var motionAlertJob: Job? = null

    private var targetDurationSeconds: Long = 25 * 60L
    private var startElapsedRealtime: Long = 0L
    private var accumulatedElapsedSeconds: Long = 0L
    private var sessionStartTime: Long = 0L

    private var movementCount: Int = 0
    private var lastMovementTimeMs: Long = 0L
    private var lightSamplesSum: Double = 0.0
    private var lightSamplesCount: Int = 0

    @Volatile
    private var isSessionConsumed: Boolean = false

    override fun start(durationMinutes: Int) {
        val safeMinutes = if (durationMinutes > 0) durationMinutes else 25
        targetDurationSeconds = safeMinutes * 60L
        accumulatedElapsedSeconds = 0L
        startElapsedRealtime = timeProvider.elapsedRealtime()
        sessionStartTime = timeProvider.currentTimeMillis()

        movementCount = 0
        lastMovementTimeMs = 0L
        lightSamplesSum = 0.0
        lightSamplesCount = 0
        isSessionConsumed = false

        _timerState.value = TimerState(
            mode = TimerMode.RUNNING,
            targetDurationSeconds = targetDurationSeconds,
            elapsedSeconds = 0L,
            remainingSeconds = targetDurationSeconds,
            progress = 0.0f,
            formattedRemainingTime = TimerState.formatTime(targetDurationSeconds),
            movementCount = 0,
            averageLightLux = null,
            currentLux = null,
            currentLightCategory = null,
            isMotionAlertActive = false,
            sessionStartTime = sessionStartTime
        )

        startSensorMonitoring()
        startTickJob()
    }

    override fun pause() {
        if (_timerState.value.mode != TimerMode.RUNNING) return

        val now = timeProvider.elapsedRealtime()
        val periodElapsed = (now - startElapsedRealtime) / 1000L
        accumulatedElapsedSeconds += periodElapsed

        stopTickJob()
        stopSensorMonitoring()

        val remaining = maxOf(0L, targetDurationSeconds - accumulatedElapsedSeconds)
        val progress = if (targetDurationSeconds > 0) {
            (accumulatedElapsedSeconds.toFloat() / targetDurationSeconds.toFloat()).coerceIn(0.0f, 1.0f)
        } else 0.0f

        _timerState.update { current ->
            current.copy(
                mode = TimerMode.PAUSED,
                elapsedSeconds = accumulatedElapsedSeconds,
                remainingSeconds = remaining,
                progress = progress,
                formattedRemainingTime = TimerState.formatTime(remaining),
                isMotionAlertActive = false
            )
        }
    }

    override fun resume() {
        if (_timerState.value.mode != TimerMode.PAUSED) return

        startElapsedRealtime = timeProvider.elapsedRealtime()

        _timerState.update { current ->
            current.copy(mode = TimerMode.RUNNING)
        }

        startSensorMonitoring()
        startTickJob()
    }

    override fun stop(): FocusSession? {
        val currentMode = _timerState.value.mode
        if (currentMode == TimerMode.IDLE) return null

        if (currentMode == TimerMode.RUNNING) {
            val now = timeProvider.elapsedRealtime()
            accumulatedElapsedSeconds += (now - startElapsedRealtime) / 1000L
        }

        stopTickJob()
        stopSensorMonitoring()

        _timerState.update { current ->
            current.copy(
                mode = TimerMode.IDLE,
                elapsedSeconds = accumulatedElapsedSeconds,
                isMotionAlertActive = false
            )
        }

        return buildAndConsumeSession(SessionStatus.INTERRUPTED)
    }

    override fun complete(): FocusSession? {
        stopTickJob()
        stopSensorMonitoring()

        accumulatedElapsedSeconds = targetDurationSeconds

        val avgLux = if (lightSamplesCount > 0) (lightSamplesSum / lightSamplesCount).toFloat() else null

        _timerState.update { current ->
            current.copy(
                mode = TimerMode.COMPLETED,
                elapsedSeconds = targetDurationSeconds,
                remainingSeconds = 0L,
                progress = 1.0f,
                formattedRemainingTime = "00:00",
                averageLightLux = avgLux,
                isMotionAlertActive = false
            )
        }

        return buildAndConsumeSession(SessionStatus.COMPLETED)
    }

    override fun reset(targetDurationMinutes: Int) {
        stopTickJob()
        stopSensorMonitoring()

        val safeMinutes = if (targetDurationMinutes > 0) targetDurationMinutes else 25
        targetDurationSeconds = safeMinutes * 60L
        accumulatedElapsedSeconds = 0L
        movementCount = 0
        lastMovementTimeMs = 0L
        lightSamplesSum = 0.0
        lightSamplesCount = 0
        isSessionConsumed = false

        _timerState.value = TimerState(
            mode = TimerMode.IDLE,
            targetDurationSeconds = targetDurationSeconds,
            elapsedSeconds = 0L,
            remainingSeconds = targetDurationSeconds,
            progress = 0.0f,
            formattedRemainingTime = TimerState.formatTime(targetDurationSeconds),
            movementCount = 0,
            averageLightLux = null,
            currentLux = null,
            currentLightCategory = null,
            isMotionAlertActive = false,
            sessionStartTime = 0L
        )
    }

    @Synchronized
    override fun buildAndConsumeSession(status: SessionStatus, notes: String?): FocusSession? {
        if (isSessionConsumed) return null
        if (sessionStartTime == 0L && accumulatedElapsedSeconds == 0L) return null

        isSessionConsumed = true
        val current = _timerState.value
        val endTime = timeProvider.currentTimeMillis()
        val avgLux = if (lightSamplesCount > 0) {
            (lightSamplesSum / lightSamplesCount).toFloat()
        } else {
            current.averageLightLux
        }

        val effectiveDuration = maxOf(accumulatedElapsedSeconds, current.elapsedSeconds)

        return FocusSession(
            id = 0L,
            startTime = sessionStartTime,
            endTime = endTime,
            durationSeconds = effectiveDuration,
            status = status,
            notes = notes,
            movementCount = current.movementCount,
            averageLightLux = avgLux
        )
    }

    override fun dismissMotionAlert() {
        motionAlertJob?.cancel()
        _timerState.update { it.copy(isMotionAlertActive = false) }
    }

    private fun startTickJob() {
        tickJob?.cancel()
        tickJob = engineScope.launch {
            while (isActive && _timerState.value.mode == TimerMode.RUNNING) {
                delay(200L)
                val now = timeProvider.elapsedRealtime()
                val currentPeriodElapsed = (now - startElapsedRealtime) / 1000L
                val totalElapsed = accumulatedElapsedSeconds + currentPeriodElapsed
                val remaining = maxOf(0L, targetDurationSeconds - totalElapsed)
                val progress = if (targetDurationSeconds > 0) {
                    (totalElapsed.toFloat() / targetDurationSeconds.toFloat()).coerceIn(0.0f, 1.0f)
                } else 0.0f

                _timerState.update { current ->
                    current.copy(
                        elapsedSeconds = totalElapsed,
                        remainingSeconds = remaining,
                        progress = progress,
                        formattedRemainingTime = TimerState.formatTime(remaining)
                    )
                }

                if (remaining <= 0L) {
                    complete()
                    break
                }
            }
        }
    }

    private fun stopTickJob() {
        tickJob?.cancel()
        tickJob = null
    }

    private fun startSensorMonitoring() {
        val repo = sensorRepository ?: return
        repo.startListening()

        accelerometerJob?.cancel()
        accelerometerJob = engineScope.launch {
            repo.accelerometerFlow.collect { data ->
                if (_timerState.value.mode != TimerMode.RUNNING) return@collect
                if (data.isSignificantMovement) {
                    val now = timeProvider.elapsedRealtime()
                    if (now - lastMovementTimeMs > 1200L) {
                        lastMovementTimeMs = now
                        movementCount++
                        _timerState.update {
                            it.copy(
                                movementCount = movementCount,
                                isMotionAlertActive = true
                            )
                        }
                        motionAlertJob?.cancel()
                        motionAlertJob = engineScope.launch {
                            delay(2500L)
                            _timerState.update { it.copy(isMotionAlertActive = false) }
                        }
                    }
                }
            }
        }

        lightJob?.cancel()
        lightJob = engineScope.launch {
            repo.lightFlow.collect { data ->
                if (_timerState.value.mode != TimerMode.RUNNING) return@collect
                lightSamplesSum += data.lux
                lightSamplesCount++
                val avgLux = (lightSamplesSum / lightSamplesCount).toFloat()
                _timerState.update {
                    it.copy(
                        currentLux = data.lux,
                        currentLightCategory = data.category,
                        averageLightLux = avgLux
                    )
                }
            }
        }
    }

    private fun stopSensorMonitoring() {
        accelerometerJob?.cancel()
        accelerometerJob = null
        lightJob?.cancel()
        lightJob = null
        motionAlertJob?.cancel()
        motionAlertJob = null
        sensorRepository?.stopListening()
    }
}
