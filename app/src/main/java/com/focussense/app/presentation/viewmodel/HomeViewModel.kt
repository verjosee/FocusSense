package com.focussense.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focussense.app.data.repository.SettingsRepository
import com.focussense.app.data.repository.StudySessionRepository
import com.focussense.app.domain.model.FocusSession
import com.focussense.app.domain.model.StatisticsSummary
import com.focussense.app.domain.model.toDomainList
import com.focussense.app.domain.model.toEntity
import com.focussense.app.domain.timer.FocusTimerEngine
import com.focussense.app.domain.timer.TimerState
import com.focussense.app.sensors.SensorRepository
import com.focussense.app.sensors.model.LightCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class HomeUiState(
    val todayDurationSeconds: Long = 0L,
    val todayCompletedCount: Int = 0,
    val formattedTodayDuration: String = "0m",
    val recentSessions: List<FocusSession> = emptyList(),
    val activeTimerState: TimerState = TimerState(),
    val currentLux: Float? = null,
    val currentLightCategory: LightCategory? = null,
    val accelerometerMagnitude: Float = 0f,
    val isDeviceMoving: Boolean = false,
    val isAccelerometerSupported: Boolean = true,
    val isLightSensorSupported: Boolean = true,
    val defaultFocusMinutes: Int = 25
)

class HomeViewModel(
    private val studySessionRepository: StudySessionRepository,
    private val sensorRepository: SensorRepository,
    val timerEngine: FocusTimerEngine,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            isAccelerometerSupported = sensorRepository.isAccelerometerAvailable,
            isLightSensorSupported = sensorRepository.isLightSensorAvailable
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        sensorRepository.startListening()
        loadData()
    }

    private fun loadData() {
        val startOfDay = getStartOfDayTimestamp()

        viewModelScope.launch {
            studySessionRepository.getTodayTotalDuration(startOfDay).collect { duration ->
                val seconds = duration ?: 0L
                _uiState.update { current ->
                    current.copy(
                        todayDurationSeconds = seconds,
                        formattedTodayDuration = StatisticsSummary.formatDuration(seconds)
                    )
                }
            }
        }

        viewModelScope.launch {
            studySessionRepository.getTodayCompletedCount(startOfDay).collect { count ->
                _uiState.update { current ->
                    current.copy(todayCompletedCount = count)
                }
            }
        }

        viewModelScope.launch {
            studySessionRepository.getAllSessions().collect { entities ->
                val recent = entities.take(5).toDomainList()
                _uiState.update { current ->
                    current.copy(recentSessions = recent)
                }
            }
        }

        viewModelScope.launch {
            timerEngine.timerState.collect { timerState ->
                _uiState.update { current ->
                    current.copy(activeTimerState = timerState)
                }
            }
        }

        viewModelScope.launch {
            settingsRepository.defaultDurationMinutesFlow.collect { minutes ->
                _uiState.update { current ->
                    current.copy(defaultFocusMinutes = minutes)
                }
            }
        }

        viewModelScope.launch {
            sensorRepository.lightFlow.collect { lightData ->
                _uiState.update { current ->
                    current.copy(
                        currentLux = lightData.lux,
                        currentLightCategory = lightData.category
                    )
                }
            }
        }

        viewModelScope.launch {
            sensorRepository.accelerometerFlow.collect { accData ->
                _uiState.update { current ->
                    current.copy(
                        accelerometerMagnitude = accData.magnitude,
                        isDeviceMoving = accData.isSignificantMovement
                    )
                }
            }
        }
    }

    fun startQuickFocus(durationMinutes: Int? = null) {
        val duration = durationMinutes ?: _uiState.value.defaultFocusMinutes
        timerEngine.start(duration)
    }

    fun pauseTimer() {
        timerEngine.pause()
    }

    fun resumeTimer() {
        timerEngine.resume()
    }

    fun stopTimer() {
        val session = timerEngine.stop()
        if (session != null && session.durationSeconds > 10) {
            viewModelScope.launch {
                studySessionRepository.insertSession(session.toEntity())
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorRepository.stopListening()
    }

    private fun getStartOfDayTimestamp(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}
